package com.consultamerica.hr.sentemail.controller;

import com.consultamerica.hr.sentemail.entity.SentEmail;
import com.consultamerica.hr.sentemail.service.SentEmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/resumes/api/sent-emails")
public class SentEmailController {

    private final SentEmailService service;

    public SentEmailController(SentEmailService service) {
        this.service = service;
    }

    @PostMapping("")
    public ResponseEntity<SentEmail> create(@RequestBody SentEmail sentEmail) {
        return ResponseEntity.ok(service.save(sentEmail));
    }

    @GetMapping("")
    public ResponseEntity<List<SentEmail>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("")
    public ResponseEntity<Void> clearAll() {
        service.deleteAll();
        return ResponseEntity.ok().build();
    }
}
