package com.browserfilesystem.service;

import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.repository.FileItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
/** Owns file-system rules: root normalization, parent validation, sibling uniqueness, search, and deletion. */
public class FileService {
    private final FileItemRepository fileRepository;
    private static final int SEARCH_LIMIT = 10;
    // A stable secondary sort prevents autocomplete suggestions from changing between requests.
    private static final PageRequest AUTOCOMPLETE_PAGE = PageRequest.of(
            0,
            SEARCH_LIMIT,
            Sort.by(Sort.Order.asc("normalizedName"), Sort.Order.asc("id"))
    );

    /** Returns a sorted database page of direct children, with null consistently representing the root. */
    public Page<FileItem> listFilesByParent(String parentId, int page, int size) {
        if (page < 0 || size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be positive");
        }
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.asc("normalizedName"), Sort.Order.asc("id"))
        );
        return fileRepository.findByParentId(normalizeParentId(parentId), pageable);
    }

    /** Looks up an item without translating a missing identifier into an HTTP response. */
    public Optional<FileItem> getFileById(String id) {
        return fileRepository.findById(id);
    }

    /** Creates a file after the shared parent and duplicate-name checks. */
    public FileItem createFile(String name, String parentId) {
        return createItem(name, parentId, false);
    }

    /** Creates a folder after the shared parent and duplicate-name checks. */
    public FileItem createFolder(String name, String parentId) {
        return createItem(name, parentId, true);
    }

    /** Builds an item with a UUID-backed materialized path before saving it. */
    private FileItem createItem(String name, String parentId, boolean isFolder) {
        String normalizedParentId = normalizeParentId(parentId);
        FileItem parent = findParentFolder(normalizedParentId);
        ensureNameIsAvailable(name, normalizedParentId, null);

        String id = UUID.randomUUID().toString();
        // Paths include the item's own id and a trailing slash, allowing safe prefix matching for descendants.
        String parentPath = parent == null ? "/" : parent.getPath();
        FileItem item = new FileItem(id, name, normalizedParentId, parentPath + id + "/", isFolder);
        return fileRepository.save(item);
    }

    /** Validates that a supplied parent exists and is a folder; root has no parent and returns null. */
    private FileItem findParentFolder(String parentId) {
        String normalizedParentId = normalizeParentId(parentId);
        if (normalizedParentId == null) {
            return null;
        }

        FileItem parent = fileRepository.findById(normalizedParentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent folder not found"));
        if (!parent.isFolder()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent must be a folder");
        }
        return parent;
    }

    /** Converts blank inputs from HTTP clients into the canonical null root representation. */
    private String normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? null : parentId;
    }

    /** Renames an existing item while excluding that item's current name from the duplicate check. */
    public Optional<FileItem> renameFile(String id, String newName) {
        return fileRepository.findById(id).map(file -> {
            ensureNameIsAvailable(newName, file.getParentId(), file.getId());
            FileItem updatedFile = new FileItem(
                    id,
                    newName,
                    FileItem.normalizeName(newName),
                    file.getParentId(),
                    file.getPath(),
                    file.isFolder(),
                    file.getCreatedAt(),
                    Instant.now()
            );
            return fileRepository.save(updatedFile);
        });
    }

    /** Rejects a name already used by another file or folder under the same parent. */
    private void ensureNameIsAvailable(String name, String parentId, String excludedFileId) {
        boolean duplicateExists = findFilesByNameInParent(name, parentId).stream()
                .anyMatch(file -> !file.getId().equals(excludedFileId));
        if (duplicateExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An item with this name already exists in the folder");
        }
    }

    /** Finds matching sibling names using the normalized key required by the compound unique index. */
    private List<FileItem> findFilesByNameInParent(String name, String parentId) {
        return fileRepository.findByNormalizedNameAndParentId(
                FileItem.normalizeName(name), normalizeParentId(parentId));
    }

    /** Deletes an item/subtree and reports whether the requested item existed. */
    public boolean deleteFile(String id) {
        Optional<FileItem> file = fileRepository.findById(id);
        if (file.isEmpty()) {
            return false;
        }

        // Delete the selected item and every descendant without loading a potentially large tree into memory.
        fileRepository.deleteByPathStartingWith(file.get().getPath());
        return true;
    }

    /** Performs a paginated case-insensitive exact-name search across the entire file system. */
    public Page<FileItem> searchFilesByName(String name, int page, int size) {
        if (name == null || name.trim().isEmpty()) {
            return Page.empty();
        }
        return fileRepository.findByNormalizedName(FileItem.normalizeName(name.trim()), searchPage(page, size));
    }

    /** Performs a paginated case-insensitive exact-name search among one folder's direct children. */
    public Page<FileItem> searchFilesByNameInFolder(String name, String parentId, int page, int size) {
        if (name == null || name.trim().isEmpty()) {
            return Page.empty();
        }
        return fileRepository.findByNormalizedNameAndParentId(
                FileItem.normalizeName(name.trim()), normalizeParentId(parentId), searchPage(page, size));
    }

    /** Creates a validated, deterministic MongoDB page request for exact search. */
    private PageRequest searchPage(int page, int size) {
        if (page < 0 || size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be positive");
        }
        // Pagination and sorting happen in MongoDB, not after fetching all matches into the service.
        return PageRequest.of(page, size, Sort.by(Sort.Order.asc("normalizedName"), Sort.Order.asc("id")));
    }

    /** Returns at most ten file-only prefix suggestions across all folders. */
    public List<FileItem> getAutocompleteSuggestions(String namePrefix) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNormalizedNameStartingWithAndFolderFalse(
                FileItem.normalizeName(namePrefix.trim()), AUTOCOMPLETE_PAGE).getContent();
    }

    /** Returns at most ten file-only prefix suggestions among direct children of one folder. */
    public List<FileItem> getAutocompleteSuggestionsInFolder(String namePrefix, String parentId) {
        if (namePrefix == null || namePrefix.trim().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByNormalizedNameStartingWithAndParentIdAndFolderFalse(
                FileItem.normalizeName(namePrefix.trim()), normalizeParentId(parentId), AUTOCOMPLETE_PAGE).getContent();
    }
}
