package com.browserfilesystem.dto;

import com.browserfilesystem.model.FileItem;

import java.time.Instant;

/** Public API representation of a file-system item; it intentionally excludes MongoDB-only fields. */
public record FileItemResponse(
        String id,
        String name,
        String parentId,
        boolean folder,
        Instant createdAt,
        Instant updatedAt
) {
    /** Maps the persistence entity to the stable response shape used by controllers. */
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
