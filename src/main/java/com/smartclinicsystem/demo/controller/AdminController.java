package com.smartclinicsystem.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.smartclinicsystem.demo.entity.Admin;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.AdminRepository;
import com.smartclinicsystem.demo.service.AdminService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;

    public AdminController(AdminService adminService, AdminRepository adminRepository) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
    }

    @GetMapping("/me")
    public ApiDtos.AdminView currentAdmin(@AuthenticationPrincipal UserDetails user) {
        Admin admin = adminRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin account not found."));
        return ApiDtos.AdminView.from(admin);
    }

    @GetMapping("/doctors")
    public List<ApiDtos.DoctorView> doctors() {
        return adminService.viewAllDoctors().stream().map(ApiDtos.DoctorView::from).toList();
    }

    @PostMapping("/doctors")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.DoctorView createDoctor(@Valid @RequestBody DoctorInput input) {
        requirePassword(input.password());
        return ApiDtos.DoctorView.from(adminService.createDoctor(input.toDoctor()));
    }

    @PutMapping("/doctors/{id}")
    public ApiDtos.DoctorView updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorInput input) {
        return ApiDtos.DoctorView.from(adminService.updateDoctor(id, input.toDoctor()));
    }

    @DeleteMapping("/doctors/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctor(@PathVariable Long id) {
        adminService.deleteDoctor(id);
    }

    @GetMapping("/patients")
    public List<ApiDtos.PatientView> patients() {
        return adminService.viewAllPatients().stream().map(ApiDtos.PatientView::from).toList();
    }

    @PostMapping("/patients")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.PatientView createPatient(@Valid @RequestBody PatientInput input) {
        requirePassword(input.password());
        return ApiDtos.PatientView.from(adminService.createPatient(input.toPatient()));
    }

    @PutMapping("/patients/{id}")
    public ApiDtos.PatientView updatePatient(@PathVariable Long id, @Valid @RequestBody PatientInput input) {
        return ApiDtos.PatientView.from(adminService.updatePatient(id, input.toPatient()));
    }

    @DeleteMapping("/patients/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable Long id) {
        adminService.deletePatient(id);
    }

    @GetMapping("/appointments")
    @Transactional(readOnly = true)
    public List<ApiDtos.AppointmentView> appointments() {
        return adminService.viewAllAppointments().stream().map(ApiDtos.AppointmentView::from).toList();
    }

    @PatchMapping("/appointments/{id}/cancel")
    @Transactional
    public ApiDtos.AppointmentView cancelAppointment(@PathVariable Long id) {
        return ApiDtos.AppointmentView.from(adminService.cancelAppointment(id));
    }

    @PatchMapping("/appointments/{id}/reschedule")
    @Transactional
    public ApiDtos.AppointmentView rescheduleAppointment(@PathVariable Long id,
                                                          @RequestParam LocalDate date) {
        return ApiDtos.AppointmentView.from(adminService.rescheduleAppointment(id, date));
    }

    @PostMapping("/appointments/{id}/resolve-conflict")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void resolveAppointmentConflict(@PathVariable Long id) {
        adminService.resolveAppointmentConflict(id);
    }

    @GetMapping("/prescriptions")
    public List<ApiDtos.PrescriptionView> prescriptions() {
        return adminService.viewAllPrescriptions().stream().map(ApiDtos.PrescriptionView::from).toList();
    }

    @DeleteMapping("/prescriptions/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePrescription(@PathVariable String id) {
        adminService.deletePrescription(id);
    }

    @PostMapping("/prescriptions/audit")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void auditPrescriptions() {
        adminService.auditPrescriptionCompliance();
    }

    @GetMapping("/report")
    public Map<String, Object> report() {
        return adminService.generateSystemReport();
    }

    private void requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A password is required when creating an account.");
        }
    }

    public record DoctorInput(
            @NotBlank String firstname,
            @NotBlank String lastname,
            String password,
            @NotBlank String specialisation,
            @Positive int contact,
            @NotBlank @Email String email) {
        Doctor toDoctor() {
            Doctor doctor = new Doctor();
            doctor.setFirstname(firstname);
            doctor.setLastname(lastname);
            doctor.setPassword(password);
            doctor.setSpecialisation(specialisation);
            doctor.setContact(contact);
            doctor.setEmail(email);
            return doctor;
        }
    }

    public record PatientInput(
            @NotBlank String firstname,
            @NotBlank String lastname,
            String password,
            @NotBlank String dob,
            @NotBlank String gender,
            @NotBlank String phoneno,
            @NotBlank @Email String email,
            @NotBlank String address) {
        Patient toPatient() {
            Patient patient = new Patient();
            patient.setFirstname(firstname);
            patient.setLastname(lastname);
            patient.setPassword(password);
            patient.setDob(dob);
            patient.setGender(gender);
            patient.setPhoneno(phoneno);
            patient.setEmail(email);
            patient.setAddress(address);
            return patient;
        }
    }
}
