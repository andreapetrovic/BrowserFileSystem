package com.browserfilesystem.repository;

import com.browserfilesystem.model.FileItem;
import com.mongodb.client.model.IndexOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest(properties = "spring.data.mongodb.auto-index-creation=false")
@Testcontainers(disabledWithoutDocker = true)
class FileItemRepositoryTest {
    @Container
    static final MongoDBContainer mongoDb = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDb::getReplicaSetUrl);
    }

    @Autowired
    private FileItemRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUpCollectionAndIndexes() {
        mongoTemplate.dropCollection(FileItem.class);
        MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(
                mongoTemplate.getConverter().getMappingContext());
        for (IndexDefinition index : resolver.resolveIndexFor(FileItem.class)) {
            mongoTemplate.indexOps(FileItem.class).ensureIndex(index);
        }
    }

    @Test
    void createsTheExpectedIndexesAndEnforcesSiblingNameUniqueness() {
        List<String> indexNames = mongoTemplate.indexOps(FileItem.class).getIndexInfo().stream()
                .map(index -> index.getName())
                .toList();
        assertThat(indexNames).contains("normalizedName", "path", "parentId", "uniq_parent_name");

        repository.save(item("first", "Report.pdf", null, "/first/", false));

        assertThatThrownBy(() -> repository.save(item("second", "report.pdf", null, "/second/", false)))
                .hasRootCauseInstanceOf(com.mongodb.MongoWriteException.class);
    }

    @Test
    void performsPagedFolderListingAndPrefixAutocompleteInMongo() {
        repository.saveAll(List.of(
                item("a", "Alpha.txt", null, "/a/", false),
                item("b", "Alpine.txt", null, "/b/", false),
                item("c", "Archive", null, "/c/", true)
        ));

        Page<FileItem> firstPage = repository.findByParentId(null, PageRequest.of(0, 1,
                Sort.by(Sort.Order.asc("normalizedName"))));
        Page<FileItem> suggestions = repository.findByNormalizedNameStartingWithAndFolderFalse("al",
                PageRequest.of(0, 10, Sort.by(Sort.Order.asc("normalizedName"))));

        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(suggestions.getContent()).extracting(FileItem::getName)
                .containsExactly("Alpha.txt", "Alpine.txt");
    }

    private static FileItem item(String id, String name, String parentId, String path, boolean folder) {
        return new FileItem(id, name, parentId, path, folder);
    }
}
