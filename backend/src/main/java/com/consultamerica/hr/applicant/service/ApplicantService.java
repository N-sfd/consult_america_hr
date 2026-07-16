package com.consultamerica.hr.applicant.service;

import com.consultamerica.hr.applicant.dto.ApplicantRequest;
import com.consultamerica.hr.applicant.entity.Applicant;
import com.consultamerica.hr.applicant.repository.ApplicantRepository;
import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicantService {

    private final ApplicantRepository repository;

    public ApplicantService(ApplicantRepository repository) {
        this.repository = repository;
    }

    public List<Applicant> list() {
        return repository.findAll();
    }

    public Applicant get(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Applicant not found: " + id));
    }

    public Applicant create(ApplicantRequest request) {
        Applicant applicant = new Applicant();
        apply(applicant, request);
        return repository.save(applicant);
    }

    public Applicant update(Long id, ApplicantRequest request) {
        Applicant applicant = get(id);
        apply(applicant, request);
        return repository.save(applicant);
    }

    public void delete(Long id) {
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
