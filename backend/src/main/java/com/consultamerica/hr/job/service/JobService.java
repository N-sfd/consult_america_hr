package com.consultamerica.hr.job.service;

import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import com.consultamerica.hr.job.dto.JobPostRequest;
import com.consultamerica.hr.job.entity.JobPosts;
import com.consultamerica.hr.job.repository.JobPostsRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class JobService {

    private final JobPostsRepository repository;

    public JobService(JobPostsRepository repository) {
        this.repository = repository;
    }

    public JobPosts postJob(JobPostRequest request) {
        JobPosts job = new JobPosts();
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setLocation(request.location());
        job.setEmploymentType(request.employmentType());
        job.setTechnologyStack(request.technologyStack());
        job.setClientName(request.clientName());
        job.setContactEmail(request.contactEmail());
        job.setPostedAt(parseOrNow(request.postedAt()));
        return repository.save(job);
    }

    public List<JobPosts> listJobs() {
        return repository.findAll();
    }

    public JobPosts getJob(Long id) {
        Objects.requireNonNull(id, "id is required");
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }

    private Instant parseOrNow(String value) {
        if (value == null || value.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
