package com.browserfilesystem.service;

import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.repository.FileItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileItemRepository fileRepository;
    private static final int SEARCH_LIMIT = 10;
    private static final PageRequest AUTOCOMPLETE_PAGE = PageRequest.of(
            0,
            SEARCH_LIMIT,
            Sort.by(Sort.Order.asc("normalizedName"), Sort.Order.asc("id"))
    );

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
                    FileItem.normalizeName(newName),
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
        return fileRepository.findByNormalizedNameAndParentId(
                FileItem.normalizeName(name), normalizeParentId(parentId));
    }

    public boolean deleteFile(String id) {
        Optional<FileItem> file = fileRepository.findById(id);
        if (file.isEmpty()) {
            return false;
        }

        if (file.get().isFolder()) {
            List<FileItem> children = fileRepository.findByParentId(id);
            for (FileItem child : children) {
                deleteFile(child.getId());
            }
        }
        fileRepository.deleteById(id);
        return true;
    }

    public List<FileItem> searchFilesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNormalizedName(FileItem.normalizeName(name.trim()));
    }

    public List<FileItem> searchFilesByNameInFolder(String name, String parentId) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNormalizedNameAndParentId(
                FileItem.normalizeName(name.trim()), normalizeParentId(parentId));
    }

    public List<FileItem> getAutocompleteSuggestions(String namePrefix) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNormalizedNameStartingWithAndIsFolderFalse(
                FileItem.normalizeName(namePrefix.trim()), AUTOCOMPLETE_PAGE).getContent();
    }

    public List<FileItem> getAutocompleteSuggestionsInFolder(String namePrefix, String parentId) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNormalizedNameStartingWithAndParentIdAndIsFolderFalse(
                FileItem.normalizeName(namePrefix.trim()), normalizeParentId(parentId), AUTOCOMPLETE_PAGE).getContent();
    }
}
