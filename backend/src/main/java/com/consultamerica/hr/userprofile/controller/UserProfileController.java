package com.consultamerica.hr.userprofile.controller;

import com.consultamerica.hr.userprofile.dto.UserProfileRequest;
import com.consultamerica.hr.userprofile.entity.UserProfile;
import com.consultamerica.hr.userprofile.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user-profile")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserProfile> get(@PathVariable String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @PutMapping("/{email}")
    public ResponseEntity<UserProfile> update(@PathVariable String email, @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(service.upsertByEmail(email, request));
    }

    @PostMapping("/{candidateId}/send-email-complete")
    public ResponseEntity<Void> sendEmailComplete(@PathVariable Long candidateId, @RequestBody(required = false) Map<String, Object> payload) {
        service.sendProfileCompleteNotification(candidateId, payload);
        return ResponseEntity.ok().build();
    }
}
