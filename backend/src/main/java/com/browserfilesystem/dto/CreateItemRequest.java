package com.browserfilesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Validated JSON payload shared by file and folder creation endpoints. */
public record CreateItemRequest(
        @NotBlank @Size(max = 255) String name,
        String parentId
) {
}
