package org.boot.uploader.service.impl;

import org.boot.uploader.service.JpaService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public abstract class JpaServiceImpl<T,ID> implements JpaService<T,ID> {

    public abstract JpaRepository<T, ID> getJpaRepository();

    @Override
    public Optional<T> get(ID id) {
        return this.getJpaRepository().findById(id);
    }

    @Override
    public T getOne(ID id) {
        if (id == null) {
            return null;
        } else {
            Optional<T> optionalT = this.get(id);
            return optionalT.equals(Optional.empty()) ? null : optionalT.get();
        }
    }
}
