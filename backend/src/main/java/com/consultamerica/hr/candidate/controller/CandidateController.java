package com.consultamerica.hr.candidate.controller;

import com.consultamerica.hr.resume.dto.ResumeResponse;
import com.consultamerica.hr.resume.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * "Candidates" has no distinct field set anywhere in the frontend contract — it's a read-alias
 * over Resume data, so this delegates to ResumeService rather than introducing a parallel entity.
 */
@RestController
public class CandidateController {

    private final ResumeService resumeService;

    public CandidateController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<ResumeResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        List<ResumeResponse> result = resumeService.listResumes(page, size, null)
            .map(ResumeResponse::from)
            .getContent();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/candidates/{id}")
    public ResponseEntity<ResumeResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(ResumeResponse.from(resumeService.getById(id)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<ResumeResponse>> byEmail(@PathVariable String email) {
        List<ResumeResponse> result = resumeService.findByEmail(email).stream()
            .map(ResumeResponse::from)
            .toList();
        return ResponseEntity.ok(result);
    }
}
