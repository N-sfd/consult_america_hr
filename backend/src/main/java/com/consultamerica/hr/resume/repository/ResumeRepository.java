package com.consultamerica.hr.resume.repository;

import com.consultamerica.hr.resume.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Page<Resume> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTitleContainingIgnoreCase(
        String name, String email, String title, Pageable pageable);

    List<Resume> findByEmailIgnoreCase(String email);
}
