package com.consultamerica.hr.job.controller;

import com.consultamerica.hr.job.dto.JobPostRequest;
import com.consultamerica.hr.job.entity.JobPosts;
import com.consultamerica.hr.job.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @PostMapping("/post")
    public ResponseEntity<JobPosts> post(@RequestBody JobPostRequest request) {
        return ResponseEntity.ok(service.postJob(request));
    }

    @GetMapping("")
    public ResponseEntity<List<JobPosts>> list() {
        return ResponseEntity.ok(service.listJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPosts> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getJob(id));
    }
}
