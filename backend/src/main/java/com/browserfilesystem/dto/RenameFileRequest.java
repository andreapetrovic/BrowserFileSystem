package com.browserfilesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Validated JSON payload for changing an existing item's display name. */
public record RenameFileRequest(@NotBlank @Size(max = 255) String name) {
}
