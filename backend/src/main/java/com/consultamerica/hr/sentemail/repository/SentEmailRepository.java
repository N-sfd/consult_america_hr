package com.consultamerica.hr.sentemail.repository;

import com.consultamerica.hr.sentemail.entity.SentEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentEmailRepository extends JpaRepository<SentEmail, Long> {
}
