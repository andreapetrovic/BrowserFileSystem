package com.browserfilesystem.service;

import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.repository.FileItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileItemRepository fileRepository;
    private static final int SEARCH_LIMIT = 10;

    public List<FileItem> listFilesByParent(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            List<FileItem> rootFiles = new ArrayList<>(fileRepository.findByParentId(null));
            rootFiles.addAll(fileRepository.findByParentId(""));
            return rootFiles;
        }
        return fileRepository.findByParentId(parentId);
    }

    public Optional<FileItem> getFileById(String id) {
        return fileRepository.findById(id);
    }

    public FileItem createFile(String name, String parentId) {
        FileItem file = new FileItem(name, normalizeParentId(parentId), false);
        return fileRepository.save(file);
    }

    public FileItem createFolder(String name, String parentId) {
        FileItem folder = new FileItem(name, normalizeParentId(parentId), true);
        return fileRepository.save(folder);
    }

    private String normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? null : parentId;
    }

    public Optional<FileItem> renameFile(String id, String newName) {
        return fileRepository.findById(id).map(file -> {
            FileItem updatedFile = new FileItem(
                    id,
                    newName,
                    file.getParentId(),
                    file.isFolder(),
                    file.getCreatedAt(),
                    Instant.now()
            );
            return fileRepository.save(updatedFile);
        });
    }

    public void deleteFile(String id) {
        Optional<FileItem> file = fileRepository.findById(id);
        if (file.isPresent() && file.get().isFolder()) {
            List<FileItem> children = fileRepository.findByParentId(id);
            for (FileItem child : children) {
                deleteFile(child.getId());
            }
        }
        fileRepository.deleteById(id);
    }

    public List<FileItem> searchFilesByName(String namePrefix) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNameIgnoreCaseStartingWith(namePrefix)
                .stream()
                .limit(SEARCH_LIMIT)
                .collect(Collectors.toList());
    }

    public List<FileItem> searchFilesByNameInFolder(String namePrefix, String parentId) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNameIgnoreCaseStartingWithAndParentId(namePrefix, parentId)
                .stream()
                .limit(SEARCH_LIMIT)
                .collect(Collectors.toList());
    }
}