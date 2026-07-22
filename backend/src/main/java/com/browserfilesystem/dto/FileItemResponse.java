package com.browserfilesystem.dto;

import com.browserfilesystem.model.FileItem;

import java.time.Instant;

public record FileItemResponse(
        String id,
        String name,
        String parentId,
        boolean folder,
        Instant createdAt,
        Instant updatedAt
) {
    public static FileItemResponse from(FileItem file) {
        return new FileItemResponse(
                file.getId(),
                file.getName(),
                file.getParentId(),
                file.isFolder(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }
}
