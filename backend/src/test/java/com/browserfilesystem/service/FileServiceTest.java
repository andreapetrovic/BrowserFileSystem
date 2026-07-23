package com.browserfilesystem.service;

import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.repository.FileItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/** Verifies the service's file-system rules independently from MongoDB using Mockito. */
class FileServiceTest {
    @Mock
    private FileItemRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    @Captor
    private ArgumentCaptor<FileItem> fileCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    void createsRootFileWithNullParentAndMaterializedPath() {
        when(fileRepository.findByNormalizedNameAndParentId("readme.md", null)).thenReturn(List.of());
        when(fileRepository.save(any(FileItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileItem created = fileService.createFile("README.md", "");

        assertThat(created.getParentId()).isNull();
        assertThat(created.getNormalizedName()).isEqualTo("readme.md");
        assertThat(created.getPath()).startsWith("/").endsWith("/");
        assertThat(created.isFolder()).isFalse();
        verify(fileRepository).save(fileCaptor.capture());
        assertThat(fileCaptor.getValue().getParentId()).isNull();
    }

    @Test
    void rejectsMissingOrNonFolderParents() {
        when(fileRepository.findById("missing")).thenReturn(Optional.empty());

        assertStatus(HttpStatus.NOT_FOUND, () -> fileService.createFile("a.txt", "missing"));

        when(fileRepository.findById("file-parent")).thenReturn(Optional.of(item("file-parent", "a.txt", null, "/file-parent/", false)));
        assertStatus(HttpStatus.BAD_REQUEST, () -> fileService.createFolder("child", "file-parent"));
    }

    @Test
    void rejectsDuplicateCreationAndRenameWithinTheSameFolder() {
        FileItem existing = item("existing", "Report.pdf", null, "/existing/", false);
        when(fileRepository.findByNormalizedNameAndParentId("report.pdf", null)).thenReturn(List.of(existing));

        assertStatus(HttpStatus.CONFLICT, () -> fileService.createFile("report.pdf", null));

        FileItem renamed = item("rename", "draft.txt", null, "/rename/", false);
        when(fileRepository.findById("rename")).thenReturn(Optional.of(renamed));
        assertStatus(HttpStatus.CONFLICT, () -> fileService.renameFile("rename", "REPORT.pdf"));
    }

    @Test
    void deletesExistingSubtreeAndReturnsFalseForMissingItem() {
        FileItem folder = item("folder", "Docs", null, "/folder/", true);
        when(fileRepository.findById("folder")).thenReturn(Optional.of(folder));
        when(fileRepository.findById("missing")).thenReturn(Optional.empty());

        assertThat(fileService.deleteFile("folder")).isTrue();
        assertThat(fileService.deleteFile("missing")).isFalse();

        verify(fileRepository).deleteByPathStartingWith("/folder/");
        verify(fileRepository, never()).deleteByPathStartingWith(eq("/missing/"));
    }

    @Test
    void separatesExactSearchFromPrefixAutocompleteAndLimitsAutocompleteInDatabase() {
        FileItem exact = item("exact", "Readme", null, "/exact/", false);
        FileItem suggestion = item("suggestion", "Reader", null, "/suggestion/", false);
        when(fileRepository.findByNormalizedName(eq("readme"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(exact)));
        when(fileRepository.findByNormalizedNameStartingWithAndFolderFalse(eq("rea"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(suggestion)));

        assertThat(fileService.searchFilesByName("README", 0, 100).getContent()).containsExactly(exact);
        assertThat(fileService.getAutocompleteSuggestions("rea")).containsExactly(suggestion);

        verify(fileRepository).findByNormalizedNameStartingWithAndFolderFalse(eq("rea"), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("normalizedName")).isNotNull();
        verify(fileRepository, never()).findByNormalizedName(eq("rea"), any(Pageable.class));
    }

    /** Builds compact service-test fixtures with the fields required by path and folder rules. */
    private static FileItem item(String id, String name, String parentId, String path, boolean folder) {
        return new FileItem(id, name, parentId, path, folder);
    }

    /** Asserts that a service rule is exposed as the intended HTTP-oriented status exception. */
    private static void assertStatus(HttpStatus status, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(status);
    }
}
