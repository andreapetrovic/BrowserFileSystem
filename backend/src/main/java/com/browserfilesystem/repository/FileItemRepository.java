package com.browserfilesystem.repository;

import com.browserfilesystem.model.FileItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/** Spring Data MongoDB queries used by the service; pageable methods push limits and sorting into MongoDB. */
public interface FileItemRepository extends MongoRepository<FileItem, String> {
    /** Finds only direct children of a folder; null parentId represents the root. */
    Page<FileItem> findByParentId(String parentId, Pageable pageable);
    /** Deletes a complete subtree by matching the selected item's materialized-path prefix. */
    long deleteByPathStartingWith(String path);
    /** Supports duplicate-name checks across all folders. */
    List<FileItem> findByNormalizedName(String normalizedName);
    /** Supports duplicate-name checks within a single parent folder. */
    List<FileItem> findByNormalizedNameAndParentId(String normalizedName, String parentId);
    /** Performs a paginated exact-name search across all folders. */
    Page<FileItem> findByNormalizedName(String normalizedName, Pageable pageable);
    /** Performs a paginated exact-name search within a single parent folder. */
    Page<FileItem> findByNormalizedNameAndParentId(String normalizedName, String parentId, Pageable pageable);
    /** Returns file-only prefix suggestions; folders are deliberately excluded from autocomplete. */
    Page<FileItem> findByNormalizedNameStartingWithAndFolderFalse(String namePrefix, Pageable pageable);
    /** Returns file-only prefix suggestions restricted to a parent folder. */
    Page<FileItem> findByNormalizedNameStartingWithAndParentIdAndFolderFalse(
            String namePrefix, String parentId, Pageable pageable);
}
