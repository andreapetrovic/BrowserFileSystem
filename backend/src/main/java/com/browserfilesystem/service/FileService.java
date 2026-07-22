package com.browserfilesystem.service;

import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.repository.FileItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileItemRepository fileRepository;
    private static final int SEARCH_LIMIT = 10;

    public List<FileItem> listFilesByParent(String parentId) {
        return fileRepository.findByParentId(normalizeParentId(parentId));
    }

    public Optional<FileItem> getFileById(String id) {
        return fileRepository.findById(id);
    }

    public FileItem createFile(String name, String parentId) {
        String normalizedParentId = validateAndNormalizeParentId(parentId);
        ensureNameIsAvailable(name, normalizedParentId, null);
        FileItem file = new FileItem(name, normalizedParentId, false);
        return fileRepository.save(file);
    }

    public FileItem createFolder(String name, String parentId) {
        String normalizedParentId = validateAndNormalizeParentId(parentId);
        ensureNameIsAvailable(name, normalizedParentId, null);
        FileItem folder = new FileItem(name, normalizedParentId, true);
        return fileRepository.save(folder);
    }

    private String validateAndNormalizeParentId(String parentId) {
        String normalizedParentId = normalizeParentId(parentId);
        if (normalizedParentId == null) {
            return null;
        }

        FileItem parent = fileRepository.findById(normalizedParentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent folder not found"));
        if (!parent.isFolder()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent must be a folder");
        }
        return normalizedParentId;
    }

    private String normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? null : parentId;
    }

    public Optional<FileItem> renameFile(String id, String newName) {
        return fileRepository.findById(id).map(file -> {
            ensureNameIsAvailable(newName, file.getParentId(), file.getId());
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

    private void ensureNameIsAvailable(String name, String parentId, String excludedFileId) {
        boolean duplicateExists = findFilesByNameInParent(name, parentId).stream()
                .anyMatch(file -> !file.getId().equals(excludedFileId));
        if (duplicateExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An item with this name already exists in the folder");
        }
    }

    private List<FileItem> findFilesByNameInParent(String name, String parentId) {
        return fileRepository.findByNameIgnoreCaseAndParentId(name, normalizeParentId(parentId));
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

    public List<FileItem> searchFilesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNameIgnoreCase(name.trim());
    }

    public List<FileItem> searchFilesByNameInFolder(String name, String parentId) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNameIgnoreCaseAndParentId(name.trim(), normalizeParentId(parentId));
    }

    public List<FileItem> getAutocompleteSuggestions(String namePrefix) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNameIgnoreCaseStartingWithAndIsFolderFalse(namePrefix.trim())
                .stream()
                .limit(SEARCH_LIMIT)
                .collect(Collectors.toList());
    }

    public List<FileItem> getAutocompleteSuggestionsInFolder(String namePrefix, String parentId) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNameIgnoreCaseStartingWithAndParentIdAndIsFolderFalse(namePrefix.trim(),
                        normalizeParentId(parentId))
                .stream()
                .limit(SEARCH_LIMIT)
                .collect(Collectors.toList());
    }
}
