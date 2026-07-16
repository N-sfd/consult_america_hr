package com.consultamerica.hr.resume.service;

import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import com.consultamerica.hr.common.util.FileValidationUtil;
import com.consultamerica.hr.mail.EmailService;
import com.consultamerica.hr.resume.entity.Resume;
import com.consultamerica.hr.resume.repository.ResumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final EmailService emailService;

    public ResumeService(ResumeRepository resumeRepository, EmailService emailService) {
        this.resumeRepository = resumeRepository;
        this.emailService = emailService;
    }

    public Resume createResume(MultipartFile file, String name, String email, String contact,
                                String title, String summary, String visaStatus, String linkedln) {
        FileValidationUtil.validate(file);

        Resume resume = new Resume();
        resume.setName(FileValidationUtil.normalize(name));
        resume.setEmail(FileValidationUtil.normalize(email));
        resume.setContact(FileValidationUtil.normalize(contact));
        resume.setTitle(FileValidationUtil.normalize(title));
        resume.setSummary(FileValidationUtil.normalize(summary));
        resume.setVisaStatus(FileValidationUtil.normalize(visaStatus));
        resume.setLinkedln(FileValidationUtil.normalize(linkedln));
        resume.setCreatedAt(Instant.now());
        applyFile(resume, file);

        return resumeRepository.save(resume);
    }

    public Resume updateResume(Long id, MultipartFile file, String name, String email, String title,
                                String visaStatus, String linkedln, String summary) {
        Resume resume = getById(id);
        resume.setName(FileValidationUtil.normalize(name));
        resume.setEmail(FileValidationUtil.normalize(email));
        resume.setTitle(FileValidationUtil.normalize(title));
        resume.setVisaStatus(FileValidationUtil.normalize(visaStatus));
        resume.setLinkedln(FileValidationUtil.normalize(linkedln));
        resume.setSummary(FileValidationUtil.normalize(summary));

        if (file != null && !file.isEmpty()) {
            FileValidationUtil.validate(file);
            applyFile(resume, file);
        }

        return resumeRepository.save(resume);
    }

    public Page<Resume> listResumes(int page, int size, String search) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), size > 0 ? size : 20);
        if (search == null || search.isBlank()) {
            return resumeRepository.findAll(pageRequest);
        }
        return resumeRepository
            .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTitleContainingIgnoreCase(
                search, search, search, pageRequest);
    }

    public Resume getById(Long id) {
        return resumeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id));
    }

    public List<Resume> findByEmail(String email) {
        return resumeRepository.findByEmailIgnoreCase(email);
    }

    public void deleteResume(Long id) {
        if (!resumeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resume not found: " + id);
        }
        resumeRepository.deleteById(id);
    }

    public void sendProfile(Long id, String recipientEmail, String subject, String customMessage) {
        Resume resume = getById(id);
        emailService.sendResumeProfile(recipientEmail, subject, customMessage,
            resume.getFileBytes(), resume.getFileName(), resume.getFileContentType());
    }

    private void applyFile(Resume resume, MultipartFile file) {
        try {
            resume.setFileBytes(file.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read uploaded file", e);
        }
        resume.setFileName(file.getOriginalFilename());
        resume.setFileContentType(file.getContentType());
    }
}
