package com.consultamerica.hr.resume.controller;

import com.consultamerica.hr.resume.dto.ResumeResponse;
import com.consultamerica.hr.resume.entity.Resume;
import com.consultamerica.hr.resume.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Same resume-creation contract as {@link ResumeController#upload}, exposed at the
 * /admin/upload path used by the frontend's separate admin upload page (resume-upload.ts).
 * Delegates to the same ResumeService method — no duplicated business logic.
 */
@RestController
@RequestMapping("/admin")
public class AdminResumeController {

    private final ResumeService resumeService;

    public AdminResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
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
}
