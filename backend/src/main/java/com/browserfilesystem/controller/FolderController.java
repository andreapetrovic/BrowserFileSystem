package com.browserfilesystem.controller;

import com.browserfilesystem.dto.CreateItemRequest;
import com.browserfilesystem.dto.FileItemResponse;
import com.browserfilesystem.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileItemResponse> createFolder(@Valid @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileItemResponse.from(fileService.createFolder(request.name(), request.parentId())));
    }
}
