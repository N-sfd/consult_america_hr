package com.consultamerica.hr.userprofile.service;

import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import com.consultamerica.hr.mail.EmailService;
import com.consultamerica.hr.userprofile.dto.UserProfileRequest;
import com.consultamerica.hr.userprofile.entity.UserProfile;
import com.consultamerica.hr.userprofile.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;
    private final EmailService emailService;

    public UserProfileService(UserProfileRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public UserProfile getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResourceNotFoundException("No profile found for email: " + email));
    }

    public UserProfile upsertByEmail(String email, UserProfileRequest request) {
        UserProfile profile = repository.findByEmailIgnoreCase(email).orElseGet(UserProfile::new);
        profile.setEmail(email);
        profile.setName(request.name());
        profile.setVisaStatus(request.visaStatus());
        profile.setWorkAuthorization(request.workAuthorization());
        profile.setClientName1(request.clientName1());
        profile.setClientName2(request.clientName2());
        profile.setClientName3(request.clientName3());
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPrimaryAddress(request.primaryAddress());
        profile.setPrimaryPhone(request.primaryPhone());
        profile.setSecondaryAddress(request.secondaryAddress());
        profile.setSecondaryPhone(request.secondaryPhone());
        profile.setTechStack(request.techStack());
        profile.setYearsOfExperience(request.yearsOfExperience());
        profile.setLocation(request.location());
        profile.setEmploymentType(request.employmentType());
        return repository.save(profile);
    }

    public void sendProfileCompleteNotification(Long candidateId, Map<String, Object> payload) {
        String candidateEmail = payload != null && payload.get("email") != null
            ? String.valueOf(payload.get("email"))
            : null;
        if (candidateEmail == null && candidateId != null) {
            candidateEmail = repository.findById(candidateId)
                .map(up -> {
                    java.util.Objects.requireNonNull(up);
                    return up.getEmail();
                })
                .orElse(null);
        }
        if (candidateEmail != null) {
            emailService.sendProfileCompleteNotification(candidateEmail, payload);
        }
    }
}
