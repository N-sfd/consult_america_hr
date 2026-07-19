package com.consultamerica.hr.applicant.service;

import com.consultamerica.hr.applicant.dto.ApplicantRequest;
import com.consultamerica.hr.applicant.entity.Applicant;
import com.consultamerica.hr.applicant.repository.ApplicantRepository;
import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@SuppressWarnings("null")
public class ApplicantService {

    private final ApplicantRepository repository;

    public ApplicantService(ApplicantRepository repository) {
        this.repository = repository;
    }

    public List<Applicant> list() {
        return repository.findAll();
    }

    public Applicant get(Long id) {
        Objects.requireNonNull(id, "id is required");
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Applicant not found: " + id));
    }

    @SuppressWarnings("null")
    public Applicant create(ApplicantRequest request) {
        Objects.requireNonNull(request, "request is required");
        Applicant applicant = new Applicant();
        apply(applicant, request);
        applicant = java.util.Objects.requireNonNull(applicant, "applicant cannot be null");
        Applicant saved = java.util.Objects.requireNonNull(repository.save(applicant));
        return saved;
    }

    public Applicant update(Long id, ApplicantRequest request) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(request, "request is required");
        Applicant applicant = get(id);
        apply(applicant, request);
        Applicant saved = java.util.Objects.requireNonNull(repository.save(applicant));
        return saved;
    }

    public void delete(Long id) {
        Objects.requireNonNull(id, "id is required");
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Applicant not found: " + id);
        }
        repository.deleteById(id);
    }

    private void apply(Applicant applicant, ApplicantRequest request) {
        applicant.setFirstName(request.firstName());
        applicant.setMiddleName(request.middleName());
        applicant.setLastName(request.lastName());
        applicant.setVisaStatus(request.visaStatus());
        applicant.setDocuments(request.documents() != null ? request.documents() : List.of());
    }
}
