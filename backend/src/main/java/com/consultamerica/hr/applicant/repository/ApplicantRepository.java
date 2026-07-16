package com.consultamerica.hr.applicant.repository;

import com.consultamerica.hr.applicant.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}
