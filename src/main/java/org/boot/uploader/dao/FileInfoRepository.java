package org.boot.uploader.dao;

import org.boot.uploader.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface FileInfoRepository extends JpaRepository<FileInfo, String> {
    @Modifying
    @Query("delete from FileInfo f where f.id=?1")
    int deleteId(String id);


}
