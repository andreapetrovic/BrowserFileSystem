package com.browserfilesystem.controller;

import com.browserfilesystem.dto.FileItemResponse;
import com.browserfilesystem.service.FileService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @GetMapping("/list")
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

    @PostMapping("/create-file")
    public ResponseEntity<FileItemResponse> createFile(
            @RequestParam @NotBlank @Size(max = 255) String name,
            @RequestParam(required = false) String parentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileItemResponse.from(fileService.createFile(name, parentId)));
    }

    @PostMapping("/create-folder")
    public ResponseEntity<FileItemResponse> createFolder(
            @RequestParam @NotBlank @Size(max = 255) String name,
            @RequestParam(required = false) String parentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileItemResponse.from(fileService.createFolder(name, parentId)));
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<FileItemResponse> renameFile(
            @PathVariable @NotBlank String id,
            @RequestParam @NotBlank @Size(max = 255) String newName) {
        return fileService.renameFile(id, newName)
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
    public ResponseEntity<List<FileItemResponse>> searchFiles(@RequestParam @NotBlank String query) {
        return ResponseEntity.ok(fileService.searchFilesByName(query).stream()
                .map(FileItemResponse::from)
                .toList());
    }

    @GetMapping("/search/folder")
    public ResponseEntity<List<FileItemResponse>> searchFilesInFolder(
            @RequestParam @NotBlank String query,
            @RequestParam String parentId) {
        return ResponseEntity.ok(fileService.searchFilesByNameInFolder(query, parentId).stream()
                .map(FileItemResponse::from)
                .toList());
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<FileItemResponse>> autocomplete(@RequestParam @NotBlank String query) {
        return ResponseEntity.ok(fileService.getAutocompleteSuggestions(query).stream()
                .map(FileItemResponse::from)
                .toList());
    }

    @GetMapping("/autocomplete/folder")
    public ResponseEntity<List<FileItemResponse>> autocompleteInFolder(
            @RequestParam @NotBlank String query,
            @RequestParam String parentId) {
        return ResponseEntity.ok(fileService.getAutocompleteSuggestionsInFolder(query, parentId).stream()
                .map(FileItemResponse::from)
                .toList());
    }
}
