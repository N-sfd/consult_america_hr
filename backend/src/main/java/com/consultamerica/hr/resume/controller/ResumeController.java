package com.consultamerica.hr.resume.controller;

import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import com.consultamerica.hr.common.util.FileValidationUtil;
import com.consultamerica.hr.resume.dto.ResumeResponse;
import com.consultamerica.hr.resume.dto.SendProfileRequest;
import com.consultamerica.hr.resume.entity.Resume;
import com.consultamerica.hr.resume.service.ResumeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("")
    public ResponseEntity<Page<ResumeResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Page<ResumeResponse> result = resumeService.listResumes(page, size, search).map(ResumeResponse::from);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String contact,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String visaStatus,
            @RequestParam(required = false) String linkedln) {
        Resume resume = resumeService.createResume(file, name, email, contact, title, summary, visaStatus, linkedln);
        return ResponseEntity.ok(ResumeResponse.from(resume));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ResumeResponse> update(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String visaStatus,
            @RequestParam(required = false) String linkedln,
            @RequestParam(required = false) String summary) {
        Resume resume = resumeService.updateResume(id, file, name, email, title, visaStatus, linkedln, summary);
        return ResponseEntity.ok(ResumeResponse.from(resume));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        Resume resume = resumeService.getById(id);
        if (resume.getFileBytes() == null || resume.getFileBytes().length == 0) {
            throw new ResourceNotFoundException("No file stored for resume: " + id);
        }
        MediaType contentType = resume.getFileContentType() != null
            ? MediaType.parseMediaType(resume.getFileContentType())
            : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + FileValidationUtil.sanitizeFileName(resume.getFileName()) + "\"")
            .body(resume.getFileBytes());
    }

    @PostMapping("/{id}/send-profile")
    public ResponseEntity<Void> sendProfile(@PathVariable Long id, @RequestBody SendProfileRequest request) {
        resumeService.sendProfile(id, request.recipientEmail(), request.subject(), request.customMessage());
        return ResponseEntity.ok().build();
    }
}
