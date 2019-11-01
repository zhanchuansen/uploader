package org.boot.uploader.service;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface JpaService<T,ID> {

    Optional<T> get(ID id);
    T getOne(ID id);

    ResponseEntity deleteById(ID id);
    List<T> findAll();
}
