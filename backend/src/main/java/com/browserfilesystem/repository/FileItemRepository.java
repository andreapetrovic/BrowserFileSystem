package com.browserfilesystem.repository;

import com.browserfilesystem.model.FileItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileItemRepository extends MongoRepository<FileItem, String> {
    Page<FileItem> findByParentId(String parentId, Pageable pageable);
    long deleteByPathStartingWith(String path);
    List<FileItem> findByNormalizedName(String normalizedName);
    List<FileItem> findByNormalizedNameAndParentId(String normalizedName, String parentId);
    Page<FileItem> findByNormalizedName(String normalizedName, Pageable pageable);
    Page<FileItem> findByNormalizedNameAndParentId(String normalizedName, String parentId, Pageable pageable);
    Page<FileItem> findByNormalizedNameStartingWithAndFolderFalse(String namePrefix, Pageable pageable);
    Page<FileItem> findByNormalizedNameStartingWithAndParentIdAndFolderFalse(
            String namePrefix, String parentId, Pageable pageable);
}
