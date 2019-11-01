package org.boot.uploader.service;

import org.boot.uploader.model.FileInfo;

public interface FileInfoService extends JpaService<FileInfo, String>{

    FileInfo addFileInfo(FileInfo fileInfo);
}
