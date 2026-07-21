package com.browserfilesystem.service;

import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.repository.FileItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileItemRepository fileRepository;

    public List<FileItem> listFilesByParent(String parentId) {
        if (parentId == null) {
            return fileRepository.findByParentId(null);
        }
        return fileRepository.findByParentId(parentId);
    }

    public Optional<FileItem> getFileById(String id) {
        return fileRepository.findById(id);
    }

    public FileItem createFile(String name, String parentId) {
        FileItem file = new FileItem(name, parentId, false);
        return fileRepository.save(file);
    }

    public FileItem createFolder(String name, String parentId) {
        FileItem folder = new FileItem(name, parentId, true);
        return fileRepository.save(folder);
    }

    public Optional<FileItem> renameFile(String id, String newName) {
        return fileRepository.findById(id).map(file -> {
            file.setName(newName);
            file.setUpdatedAt(Instant.now());
            return fileRepository.save(file);
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
}