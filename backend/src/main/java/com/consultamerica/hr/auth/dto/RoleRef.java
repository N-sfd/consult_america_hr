package com.consultamerica.hr.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRef(@NotBlank String name) {
}
