package com.browserfilesystem.controller;

import com.browserfilesystem.dto.CreateItemRequest;
import com.browserfilesystem.dto.FileItemResponse;
import com.browserfilesystem.dto.RenameFileRequest;
import com.browserfilesystem.service.FileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
/** Exposes REST endpoints for file lifecycle operations, paginated listings, and search. */
public class FileController {
    private final FileService fileService;

    @GetMapping
    /** Returns one deterministic page of direct children for the requested folder (or the root). */
    public ResponseEntity<Page<FileItemResponse>> listFiles(
            @RequestParam(required = false) String parentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(fileService.listFilesByParent(parentId, page, size).map(FileItemResponse::from));
    }

    @GetMapping("/{id}")
    /** Returns a single item when its identifier exists, otherwise a 404 response. */
    public ResponseEntity<FileItemResponse> getFile(@PathVariable @NotBlank String id) {
        return fileService.getFileById(id)
                .map(FileItemResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    /** Creates a non-folder item under the supplied parent folder. */
    public ResponseEntity<FileItemResponse> createFile(@Valid @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileItemResponse.from(fileService.createFile(request.name(), request.parentId())));
    }

    @PatchMapping("/{id}")
    /** Renames an item while the service enforces sibling-name uniqueness. */
    public ResponseEntity<FileItemResponse> renameFile(@PathVariable @NotBlank String id,
                                                         @Valid @RequestBody RenameFileRequest request) {
        return fileService.renameFile(id, request.name())
                .map(FileItemResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    /** Deletes an item and, if it is a folder, its descendants. */
    public ResponseEntity<Void> deleteFile(@PathVariable @NotBlank String id) {
        return fileService.deleteFile(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    /**
     * Returns a paginated exact-name result when {@code exact=true}; otherwise returns up to ten file-only
     * autocomplete suggestions.
     */
    public ResponseEntity<?> searchFiles(
            @RequestParam @NotBlank String name,
            @RequestParam(defaultValue = "true") boolean exact,
            @RequestParam(required = false) String parentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size) {
        if (exact) {
            Page<FileItemResponse> results = (parentId == null
                    ? fileService.searchFilesByName(name, page, size)
                    : fileService.searchFilesByNameInFolder(name, parentId, page, size))
                    .map(FileItemResponse::from);
            return ResponseEntity.ok(results);
        }

        List<FileItemResponse> suggestions = (parentId == null
                ? fileService.getAutocompleteSuggestions(name)
                : fileService.getAutocompleteSuggestionsInFolder(name, parentId))
                .stream()
                .map(FileItemResponse::from)
                .toList();
        return ResponseEntity.ok(suggestions);
    }
}
