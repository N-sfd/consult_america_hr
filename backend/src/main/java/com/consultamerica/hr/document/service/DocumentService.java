package com.consultamerica.hr.document.service;

import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import com.consultamerica.hr.common.util.FileValidationUtil;
import com.consultamerica.hr.document.entity.Document;
import com.consultamerica.hr.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public Document upload(MultipartFile file, Long userId, String documentType) {
        FileValidationUtil.validate(file);

        Document document = new Document();
        document.setUserId(userId);
        document.setDocumentType(FileValidationUtil.normalize(documentType));
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        try {
            document.setFileBytes(file.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read uploaded file", e);
        }
        return repository.save(document);
    }

    public List<Document> listByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Document not found: " + id);
        }
        repository.deleteById(id);
    }
}
