package org.boot.uploader.dao;

import org.boot.uploader.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChunkRepository extends JpaRepository<Chunk, Long>, JpaSpecificationExecutor<Chunk> {
}
