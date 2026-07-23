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
public class FileController {
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<Page<FileItemResponse>> listFiles(
            @RequestParam(required = false) String parentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(fileService.listFilesByParent(parentId, page, size).map(FileItemResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileItemResponse> getFile(@PathVariable @NotBlank String id) {
        return fileService.getFileById(id)
                .map(FileItemResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FileItemResponse> createFile(@Valid @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileItemResponse.from(fileService.createFile(request.name(), request.parentId())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileItemResponse> renameFile(@PathVariable @NotBlank String id,
                                                         @Valid @RequestBody RenameFileRequest request) {
        return fileService.renameFile(id, request.name())
                .map(FileItemResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable @NotBlank String id) {
        return fileService.deleteFile(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
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
