package org.boot.uploader.service.impl;

import org.boot.uploader.dao.ChunkRepository;
import org.boot.uploader.model.Chunk;
import org.boot.uploader.model.FileInfo;
import org.boot.uploader.service.ChunkService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;


@Service
public class ChunkServiceImpl implements ChunkService {
    @Resource
    private ChunkRepository chunkRepository;

    @Override
    public void saveChunk(Chunk chunk) {
        chunkRepository.save(chunk);
    }


    /**
     * checkChunk()方法会根据文件唯一标识，和当前块数判断是否已经上传过这个块。
     */
    @Override
    public boolean checkChunk(String identifier, Integer chunkNumber) {
        Specification<Chunk> specification = (Specification<Chunk>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("identifier"), identifier));
            predicates.add(criteriaBuilder.equal(root.get("chunkNumber"), chunkNumber));

            return criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
        };

        return chunkRepository.findOne(specification).orElse(null) == null;
    }



}
