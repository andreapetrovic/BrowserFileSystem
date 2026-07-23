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
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files")
// A case-insensitive name must be unique among siblings, regardless of whether it is a file or folder.
@CompoundIndex(name = "uniq_parent_name", def = "{'parentId': 1, 'normalizedName': 1}", unique = true)
/** MongoDB entity representing either a file or a folder in the virtual file system. */
public class FileItem {
    @Id
    private String id;
    private String name;

    // Stored lowercase so exact and prefix searches use indexed equality/prefix queries instead of regexes.
    @Indexed
    private String normalizedName;

    @Indexed
    private String parentId; // Reference to parent folder, null if root

    // Materialized path (for example /root-id/folder-id/) makes subtree deletion one database operation.
    @Indexed
    private String path;

    @JsonProperty("isFolder")
    @Field("isFolder")
    private boolean folder;
    private Instant createdAt;
    private Instant updatedAt;

    /** Creates a new item with timestamps and a normalized name; callers supply its path when it is persisted. */
    public FileItem(String name, String parentId, boolean isFolder) {
        this.name = name;
        this.normalizedName = normalizeName(name);
        this.parentId = parentId;
        this.folder = isFolder;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /** Creates a fully identified item with its materialized path, primarily for service-level creation and tests. */
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

    /** Produces the locale-independent key used for case-insensitive queries and uniqueness. */
    public static String normalizeName(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT);
    }

    /** Keeps the persisted normalized key in sync whenever the display name changes. */
    public void setName(String name) {
        this.name = name;
        this.normalizedName = normalizeName(name);
    }
}
