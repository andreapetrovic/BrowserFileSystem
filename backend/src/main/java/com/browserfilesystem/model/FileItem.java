package com.browserfilesystem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files")
@CompoundIndex(name = "uniq_parent_name", def = "{'parentId': 1, 'normalizedName': 1}", unique = true)
public class FileItem {
    @Id
    private String id;
    private String name;

    @Indexed
    private String normalizedName;

    @Indexed
    private String parentId; // Reference to parent folder, null if root

    @Indexed
    private String path;

    @JsonProperty("isFolder")
    private boolean folder;
    private Instant createdAt;
    private Instant updatedAt;

    public FileItem(String name, String parentId, boolean isFolder) {
        this.name = name;
        this.normalizedName = normalizeName(name);
        this.parentId = parentId;
        this.folder = isFolder;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public FileItem(String id, String name, String parentId, String path, boolean isFolder) {
        this.id = id;
        this.name = name;
        this.normalizedName = normalizeName(name);
        this.parentId = parentId;
        this.path = path;
        this.folder = isFolder;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static String normalizeName(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT);
    }

    public void setName(String name) {
        this.name = name;
        this.normalizedName = normalizeName(name);
    }
}
