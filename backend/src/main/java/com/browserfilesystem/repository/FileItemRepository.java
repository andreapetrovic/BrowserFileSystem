package com.browserfilesystem.repository;

import com.browserfilesystem.model.FileItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileItemRepository extends MongoRepository<FileItem, String> {
    List<FileItem> findByParentId(String parentId);
    List<FileItem> findByNameIgnoreCase(String name);
    List<FileItem> findByNameIgnoreCaseAndParentId(String name, String parentId);
    List<FileItem> findByNameIgnoreCaseStartingWithAndIsFolderFalse(String namePrefix);
    List<FileItem> findByNameIgnoreCaseStartingWithAndParentIdAndIsFolderFalse(String namePrefix, String parentId);
}
