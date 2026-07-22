package com.browserfilesystem.repository;

import com.browserfilesystem.model.FileItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileItemRepository extends MongoRepository<FileItem, String> {
    List<FileItem> findByParentId(String parentId);
    List<FileItem> findByNormalizedName(String normalizedName);
    List<FileItem> findByNormalizedNameAndParentId(String normalizedName, String parentId);
    Page<FileItem> findByNormalizedNameStartingWithAndIsFolderFalse(String namePrefix, Pageable pageable);
    Page<FileItem> findByNormalizedNameStartingWithAndParentIdAndIsFolderFalse(
            String namePrefix, String parentId, Pageable pageable);
}
