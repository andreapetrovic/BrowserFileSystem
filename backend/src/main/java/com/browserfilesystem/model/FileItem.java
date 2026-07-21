package com.browserfilesystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files")
public class FileItem {
    @Id
    private String id;
    private String name;

    @Indexed
    private String parentId; // Reference to parent folder, null if root
    private boolean isFolder;
    private Instant createdAt;
    private Instant updatedAt;

    public FileItem(String name, String parentId, boolean isFolder) {
        this.name = name;
        this.parentId = parentId;
        this.isFolder = isFolder;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}