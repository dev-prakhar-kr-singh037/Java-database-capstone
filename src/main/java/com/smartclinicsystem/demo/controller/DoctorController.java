package com.smartclinicsystem.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.service.DoctorService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;

    public DoctorController(DoctorRepository doctorRepository,
                            DoctorService doctorService) {
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;
    }

    @GetMapping("/me")
    public ApiDtos.DoctorView profile(@AuthenticationPrincipal UserDetails user) {
        return ApiDtos.DoctorView.from(currentDoctor(user));
    }

    @PutMapping("/me")
    public ApiDtos.DoctorView updateProfile(@AuthenticationPrincipal UserDetails user,
                                            @Valid @RequestBody DoctorProfileInput input) {
        Doctor existing = currentDoctor(user);
        Doctor updated = new Doctor();
        updated.setFirstname(input.firstname());
        updated.setLastname(input.lastname());
        updated.setSpecialisation(input.specialisation());
        updated.setContact(input.contact());
        updated.setEmail(existing.getEmail());
        updated.setPassword(input.password());
        return ApiDtos.DoctorView.from(doctorService.updateDoctor(existing.getDoctorid(), updated));
    }

    private Doctor currentDoctor(UserDetails user) {
        return doctorRepository.findByEmail(user.getUsername()).orElseThrow();
    }

    public record DoctorProfileInput(
            @NotBlank String firstname,
            @NotBlank String lastname,
            @NotBlank String specialisation,
            @jakarta.validation.constraints.Positive int contact,
            String password) {
    }
}
