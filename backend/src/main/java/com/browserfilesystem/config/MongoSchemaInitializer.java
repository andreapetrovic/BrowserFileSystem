package com.browserfilesystem.config;

import com.browserfilesystem.model.FileItem;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
/** Migrates legacy MongoDB documents and creates the indexes required by the file-system model. */
public class MongoSchemaInitializer {

    /** Runs the migration and index setup once the application context has a Mongo connection. */
    @Bean
    ApplicationRunner initializeMongoSchema(MongoTemplate mongoTemplate) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) {
                // Existing documents predate normalizedName and path, so migrate them before enforcing indexes.
                backfillFileFields(mongoTemplate);
                createFileIndexes(mongoTemplate);
            }
        };
    }

    /** Adds normalized names and materialized paths to documents that may have been created by older versions. */
    private void backfillFileFields(MongoTemplate mongoTemplate) {
        List<Document> documents = mongoTemplate.getCollection("files")
                .find()
                .into(new ArrayList<>());
        Map<String, Document> documentsById = new HashMap<>();
        for (Document document : documents) {
            documentsById.put(document.get("_id").toString(), document);
        }

        // Cache resolved paths so each parent chain is calculated once during the migration.
        Map<String, String> pathsById = new HashMap<>();
        for (Document document : documents) {
            String id = document.get("_id").toString();
            String name = document.getString("name");
            if (name == null) {
                throw new IllegalStateException("File item " + id + " has no name");
            }

            String parentId = normalizeParentId(document.getString("parentId"));
            String path = resolvePath(id, documentsById, pathsById, new HashSet<>());
            mongoTemplate.getCollection("files").updateOne(
                    Filters.eq("_id", document.get("_id")),
                    Updates.combine(
                            Updates.set("parentId", parentId),
                            Updates.set("normalizedName", FileItem.normalizeName(name)),
                            Updates.set("path", path)
                    )
            );
        }
    }

    /** Resolves one item's complete path while detecting missing parents and hierarchy cycles. */
    private String resolvePath(
            String id,
            Map<String, Document> documentsById,
            Map<String, String> pathsById,
            Set<String> ancestors) {
        if (pathsById.containsKey(id)) {
            return pathsById.get(id);
        }
        if (!ancestors.add(id)) {
            // A cyclic hierarchy cannot be represented by a materialized path.
            throw new IllegalStateException("Cycle detected in file hierarchy at " + id);
        }

        Document document = documentsById.get(id);
        String parentId = normalizeParentId(document.getString("parentId"));
        String parentPath = "/";
        if (parentId != null) {
            if (!documentsById.containsKey(parentId)) {
                throw new IllegalStateException("Parent folder " + parentId + " does not exist for item " + id);
            }
            parentPath = resolvePath(parentId, documentsById, pathsById, ancestors);
        }
        ancestors.remove(id);

        String path = parentPath + id + "/";
        pathsById.put(id, path);
        return path;
    }

    /** Creates all indexes declared on {@link FileItem}, including the sibling-name uniqueness constraint. */
    private void createFileIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps(FileItem.class);
        MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(
                mongoTemplate.getConverter().getMappingContext());
        // Resolve @Indexed and @CompoundIndex annotations rather than duplicating index definitions here.
        for (IndexDefinition index : resolver.resolveIndexFor(FileItem.class)) {
            indexOperations.ensureIndex(index);
        }
    }

    /** Uses null as the single representation of the root folder. */
    private String normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? null : parentId;
    }
}
