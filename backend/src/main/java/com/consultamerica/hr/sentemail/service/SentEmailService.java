package com.consultamerica.hr.sentemail.service;

import com.consultamerica.hr.sentemail.entity.SentEmail;
import com.consultamerica.hr.sentemail.repository.SentEmailRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SentEmailService {

    private final SentEmailRepository repository;

    public SentEmailService(SentEmailRepository repository) {
        this.repository = repository;
    }

    public SentEmail save(SentEmail incoming) {
        incoming.setId(null);
        if (incoming.getDate() == null) {
            incoming.setDate(Instant.now());
        }
        return repository.save(incoming);
    }

    public List<SentEmail> findAll() {
        return repository.findAll();
    }

    public void deleteAll() {
        repository.deleteAllInBatch();
    }
}
