package com.browserfilesystem.controller;

import com.browserfilesystem.exception.GlobalExceptionHandler;
import com.browserfilesystem.model.FileItem;
import com.browserfilesystem.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FileController.class, FolderController.class})
@Import(GlobalExceptionHandler.class)
class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    void listsAPageOfFiles() throws Exception {
        FileItem file = item("1", "README.md", null, "/1/", false);
        when(fileService.listFilesByParent(eq(null), eq(0), eq(100)))
                .thenReturn(new PageImpl<>(List.of(file), PageRequest.of(0, 100), 1));

        mockMvc.perform(get("/api/files?page=0&size=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("README.md"))
                .andExpect(jsonPath("$.content[0].folder").value(false));
    }

    @Test
    void validatesRequestBodiesAndPagination() throws Exception {
        mockMvc.perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/files?page=-1&size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void mapsNotFoundAndDomainErrorsToHttpResponses() throws Exception {
        when(fileService.getFileById("missing")).thenReturn(Optional.empty());
        when(fileService.createFile("duplicate.txt", null))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate name"));
        when(fileService.deleteFile("missing")).thenReturn(false);

        mockMvc.perform(get("/api/files/missing")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/files/missing")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"duplicate.txt\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Duplicate name"));
    }

    @Test
    void supportsJsonCreationRenameAndSearch() throws Exception {
        FileItem folder = item("folder", "Docs", null, "/folder/", true);
        FileItem renamed = item("file", "renamed.txt", null, "/file/", false);
        when(fileService.createFolder("Docs", null)).thenReturn(folder);
        when(fileService.renameFile("file", "renamed.txt")).thenReturn(Optional.of(renamed));
        when(fileService.searchFilesByName("renamed.txt")).thenReturn(List.of(renamed));

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Docs\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.folder").value(true));
        mockMvc.perform(patch("/api/files/file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"renamed.txt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("renamed.txt"));
        mockMvc.perform(get("/api/files/search?name=renamed.txt&exact=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("renamed.txt"));
    }

    private static FileItem item(String id, String name, String parentId, String path, boolean folder) {
        return new FileItem(id, name, parentId, path, folder);
    }
}
