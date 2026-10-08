package com.smartclinicsystem.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.PatientRepository;
import com.smartclinicsystem.demo.service.PatientService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientRepository patientRepository;
    private final PatientService patientService;

    public PatientController(PatientRepository patientRepository,
                             PatientService patientService) {
        this.patientRepository = patientRepository;
        this.patientService = patientService;
    }

    @GetMapping("/me")
    public ApiDtos.PatientView profile(@AuthenticationPrincipal UserDetails user) {
        return ApiDtos.PatientView.from(currentPatient(user));
    }

    @PutMapping("/me")
    public ApiDtos.PatientView updateProfile(@AuthenticationPrincipal UserDetails user,
                                             @Valid @RequestBody PatientProfileInput input) {
        Patient existing = currentPatient(user);
        Patient updated = new Patient();
        updated.setFirstname(input.firstname());
        updated.setLastname(input.lastname());
        updated.setPassword(input.password());
        updated.setDob(input.dob());
        updated.setGender(input.gender());
        updated.setPhoneno(input.phoneno());
        updated.setEmail(existing.getEmail());
        updated.setAddress(input.address());
        return ApiDtos.PatientView.from(patientService.updatePatient(existing.getPatientid(), updated));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/me")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal UserDetails user) {
        patientService.deletePatient(currentPatient(user).getPatientid());
    }

    @GetMapping("/doctors")
    public List<ApiDtos.DoctorView> searchDoctors(@RequestParam String name) {
        return patientService.searchDoctorsByName(name).stream().map(ApiDtos.DoctorView::from).toList();
    }

    private Patient currentPatient(UserDetails user) {
        return patientRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient account not found."));
    }

    public record PatientProfileInput(
            @NotBlank String firstname,
            @NotBlank String lastname,
            String password,
            @NotBlank String dob,
            @NotBlank String gender,
            @NotBlank String phoneno,
            @NotBlank String address) {
    }

}
