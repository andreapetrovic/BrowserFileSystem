package com.browserfilesystem.controller;

import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {
    private final FileService fileService;

    @GetMapping("/list")
    public ResponseEntity<List<FileItem>> listFiles(@RequestParam(required = false) String parentId) {
        return ResponseEntity.ok(fileService.listFilesByParent(parentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileItem> getFile(@PathVariable String id) {
        return fileService.getFileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create-file")
    public ResponseEntity<FileItem> createFile(
            @RequestParam String name,
            @RequestParam(required = false) String parentId) {
        FileItem file = fileService.createFile(name, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(file);
    }

    @PostMapping("/create-folder")
    public ResponseEntity<FileItem> createFolder(
            @RequestParam String name,
            @RequestParam(required = false) String parentId) {
        FileItem folder = fileService.createFolder(name, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<FileItem> renameFile(
            @PathVariable String id,
            @RequestParam String newName) {
        return fileService.renameFile(id, newName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileItem>> searchFiles(@RequestParam String query) {
        return ResponseEntity.ok(fileService.searchFilesByName(query));
    }

    @GetMapping("/search/folder")
    public ResponseEntity<List<FileItem>> searchFilesInFolder(
            @RequestParam String query,
            @RequestParam String parentId) {
        return ResponseEntity.ok(fileService.searchFilesByNameInFolder(query, parentId));
    }
}
