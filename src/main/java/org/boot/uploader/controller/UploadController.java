package org.boot.uploader.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.boot.uploader.model.Chunk;
import org.boot.uploader.model.FileInfo;
import org.boot.uploader.service.ChunkService;
import org.boot.uploader.service.FileInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.boot.uploader.util.FileUtils.generatePath;
import static org.boot.uploader.util.FileUtils.merge;


@RestController
@RequestMapping("/uploader")
@Slf4j
public class UploadController {

    @Value("${prop.upload-folder}")
    private String uploadFolder;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private ChunkService chunkService;

    /**
     * 上传并存储文件块，需要对文件块名进行编号，再存储在指定路径下
     */
    @PostMapping("/chunk")
    public ResponseEntity uploadChunk(Chunk chunk) {
        MultipartFile file = chunk.getFile();
        log.debug("file originName: {}, chunkNumber: {}", file.getOriginalFilename(), chunk.getChunkNumber());

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(generatePath(uploadFolder, chunk));
            //文件写入指定路径
            Files.write(path, bytes);
            log.debug("文件 {} 写入成功, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
            chunkService.saveChunk(chunk);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity("后端异常", HttpStatus.CONFLICT);
        }
    }

    /**
     * 根据文件标识和文件块检查文件块是否存在
     * 前端上传之前会先进行检测，如果此文件块已经上传过，就可以实现断点和快传
     */
    @GetMapping("/chunk")
    public Object checkChunk(Chunk chunk, HttpServletResponse response) {
        if (chunkService.checkChunk(chunk.getIdentifier(), chunk.getChunkNumber())) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        return chunk;
    }

    /**
     * 合并文件，在所有块上传完毕后，前端会调用此接口进行制定文件的合并。其中的merge方法是会遍历指定路径下的文件块，并且按照文件名中的数字进行排序后，再合并成一个文件，否则合并后的文件会无法使用
     */
    @PostMapping("/mergeFile")
    public ResponseEntity mergeFile(@RequestBody FileInfo fileInfo) {
        String filename = fileInfo.getFilename();
        String file = uploadFolder + "/" + fileInfo.getUniqueIdentifier() + "/" + filename;
        String folder = uploadFolder + "/" + fileInfo.getUniqueIdentifier();
        merge(file, folder, filename);
        fileInfo.setLocation(file);
        return ResponseEntity.ok(fileInfoService.addFileInfo(fileInfo));
    }

    /**
     * 根据多个ID进行查询
     */
    @GetMapping(value = "/getFileInfoById")
    public ResponseEntity getFileInfoById(String ids) {
        if (ids == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        String[] str = ids.split(",");
        List<FileInfo> list = new ArrayList<>();
        for (int i = 0; i < str.length; i++) {
            try {
                FileInfo fileInfo = fileInfoService.getOne(str[i]);
                list.add(fileInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(list);
    }


    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestParam("id") String id) {
        return ResponseEntity.ok(fileInfoService.deleteById(id));
    }

    /**
     * 下载单个文件
     */
    @GetMapping(value = "/download")
    public ResponseEntity<byte[]> downloadfile(@RequestParam("fileId") String fileId) throws IOException {
        Optional<FileInfo> optional = fileInfoService.get(fileId);
        if (optional == null) {
            return new ResponseEntity("没有对应的文件", HttpStatus.CONFLICT);
        }
        FileInfo fileInfo = optional.get();
        File file = new File(fileInfo.getLocation());  //文件路径
        if (file.exists()) { //文件目录是否存在
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", new String(file.getName().getBytes("UTF-8"), "ISO8859-1"));
            return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
        } else {
            return new ResponseEntity("文件不存在", HttpStatus.CONFLICT);
        }
    }

    /**
     * 多个文件下载打包
     */
    @GetMapping(value = "/downloadZip")
    public void downloadZip(@RequestParam("fileIds") String fileIds, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(fileIds)) {
            return;
        }

        String[] spilts = fileIds.split("\\,");

        if (spilts.length > 1) {   //多个文件打包下载
            String fileName = "打包文件.zip";
            fileName = URLEncoder.encode(fileName, "UTF-8"); //防止中文乱码

            response.reset();
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.addHeader("filename", fileName);

            ZipOutputStream zipOutputStream = null;
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED); //设置压缩方法

            DataOutputStream dataOutputStream = null;

            for (String fileId : spilts) {
                Optional<FileInfo> optional = fileInfoService.get(fileId);
                if (!optional.isPresent()) {
                    continue;
                }
                FileInfo fileInfo = optional.get();
                File file = new File(fileInfo.getLocation());
                if (!file.exists()) { //文件目录是否存在
                    continue;
                }
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                    dataOutputStream = new DataOutputStream(zipOutputStream);
                    FileInputStream fileis = new FileInputStream(file);
                    byte[] buf = new byte[2048];
                    int len;
                    while ((len = fileis.read(buf)) != -1) {
                        dataOutputStream.write(buf, 0, len);
                    }
                    fileis.close();
                    zipOutputStream.closeEntry();
                } catch (IOException ex) {
                    continue;
                }
            }
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.flush();
                    dataOutputStream.close();
                }
                zipOutputStream.close();
            } catch (IOException ex) {
                return;
            }
        } else {   //单个文件下载   第二种方法
            Optional<FileInfo> optional = fileInfoService.get(spilts[0]);
            if (!optional.isPresent()) {
                return;
            }
            FileInfo fileInfo = optional.get();
            File file = new File(fileInfo.getLocation());
            if (file.exists()) { //文件目录是否存在
                String fileName = file.getName();
                try {
                    fileName = URLEncoder.encode(fileName, "UTF-8"); //防止中文乱码
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.reset();
                response.setContentType("application/force-download");
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                response.setContentLength(Integer.parseInt(String.valueOf(fileInfo.getTotalSize())));
                response.addHeader("filename", fileName);
                FileInputStream fis = null; //文件输入流
                BufferedInputStream bis = null;
                OutputStream os; //输出流
                try {
                    os = response.getOutputStream();
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    byte[] buf = new byte[2048];
                    int len = bis.read(buf);
                    while (len != -1) {
                        os.write(buf);
                        len = bis.read(buf);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    bis.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                return;
            }

        }
    }
}