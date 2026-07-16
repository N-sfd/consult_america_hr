package com.consultamerica.hr.applicant.dto;

import java.util.List;

public record ApplicantRequest(
    String firstName,
    String middleName,
    String lastName,
    String visaStatus,
    List<String> documents
) {
}
