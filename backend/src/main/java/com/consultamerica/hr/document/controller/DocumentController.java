package com.consultamerica.hr.document.controller;

import com.consultamerica.hr.document.dto.DocumentResponse;
import com.consultamerica.hr.document.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponse>> listByUser(@PathVariable Long userId) {
        List<DocumentResponse> result = service.listByUser(userId).stream()
            .map(DocumentResponse::from)
            .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId,
            @RequestParam(required = false) String documentType) {
        return ResponseEntity.ok(DocumentResponse.from(service.upload(file, userId, documentType)));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        MediaType mt = MediaType.TEXT_PLAIN;
        java.util.Objects.requireNonNull(mt);
        return ResponseEntity.ok()
            .contentType(mt)
            .body("Document deleted successfully");
    }
}
