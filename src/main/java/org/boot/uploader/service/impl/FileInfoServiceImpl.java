package org.boot.uploader.service.impl;

import org.boot.uploader.dao.FileInfoRepository;
import org.boot.uploader.model.FileInfo;
import org.boot.uploader.service.FileInfoService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;


@Service
public class FileInfoServiceImpl extends JpaServiceImpl<FileInfo, java.lang.String> implements FileInfoService {

    @Resource
    private FileInfoRepository fileInfoRepository;
    @Override
    public JpaRepository<FileInfo, java.lang.String> getJpaRepository() { return fileInfoRepository; }

    @Override
    public FileInfo addFileInfo(FileInfo fileInfo) {
        return fileInfoRepository.save(fileInfo);
    }

    @Override
    @Transactional
    public ResponseEntity deleteById(String id) {
        int result=fileInfoRepository.deleteId(id);
        if(result>0){
          return new ResponseEntity(HttpStatus.OK);
          }
        return new ResponseEntity("删除失败",HttpStatus.FORBIDDEN);
    }

    @Override
    public List<FileInfo> findAll() {
        return fileInfoRepository.findAll();
    }
}
