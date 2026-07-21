package com.browserfilesystem.repository;

import com.browserfilesystem.model.FileItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileItemRepository extends MongoRepository<FileItem, String> {
    List<FileItem> findByParentId(String parentId);
    List<FileItem> findByNameIgnoreCaseStartingWith(String namePrefix);
    List<FileItem> findByNameIgnoreCaseStartingWithAndParentId(String namePrefix, String parentId);
}
