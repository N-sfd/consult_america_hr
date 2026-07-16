package com.consultamerica.hr.auth.dto;

public record LoginResponse(Long userId, String email, String name, String role) {
}
