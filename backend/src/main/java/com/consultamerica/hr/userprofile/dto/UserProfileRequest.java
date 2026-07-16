package com.consultamerica.hr.userprofile.dto;

public record UserProfileRequest(
    String email,
    String name,
    String visaStatus,
    String workAuthorization,
    String clientName1,
    String clientName2,
    String clientName3,
    String firstName,
    String lastName,
    String primaryAddress,
    String primaryPhone,
    String secondaryAddress,
    String secondaryPhone,
    String techStack,
    Integer yearsOfExperience,
    String location,
    String employmentType
) {
}
