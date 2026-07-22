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
public class MongoSchemaInitializer {

    @Bean
    ApplicationRunner initializeMongoSchema(MongoTemplate mongoTemplate) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) {
                backfillFileFields(mongoTemplate);
                createFileIndexes(mongoTemplate);
            }
        };
    }

    private void backfillFileFields(MongoTemplate mongoTemplate) {
        List<Document> documents = mongoTemplate.getCollection("files")
                .find()
                .into(new ArrayList<>());
        Map<String, Document> documentsById = new HashMap<>();
        for (Document document : documents) {
            documentsById.put(document.get("_id").toString(), document);
        }

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

    private String resolvePath(
            String id,
            Map<String, Document> documentsById,
            Map<String, String> pathsById,
            Set<String> ancestors) {
        if (pathsById.containsKey(id)) {
            return pathsById.get(id);
        }
        if (!ancestors.add(id)) {
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

    private void createFileIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps(FileItem.class);
        MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(
                mongoTemplate.getConverter().getMappingContext());
        for (IndexDefinition index : resolver.resolveIndexFor(FileItem.class)) {
            indexOperations.ensureIndex(index);
        }
    }

    private String normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? null : parentId;
    }
}
