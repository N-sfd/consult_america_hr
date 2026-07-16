package com.consultamerica.hr.common.config;

import com.consultamerica.hr.auth.entity.Role;
import com.consultamerica.hr.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitConfig {

    @org.springframework.context.annotation.Bean
    public CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            List.of("CANDIDATE", "ADMIN", "RECRUITER").forEach(name -> {
                if (roleRepository.findByNameIgnoreCase(name).isEmpty()) {
                    Role role = new Role();
                    role.setName(name);
                    roleRepository.save(role);
                }
            });
        };
    }
}
