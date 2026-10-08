package com.smartclinicsystem.demo.controller;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.PatientRepository;
import com.smartclinicsystem.demo.service.AppointmentService;
import com.smartclinicsystem.demo.service.PatientService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final AppointmentService appointmentService;

    public PatientController(PatientRepository patientRepository,
                             PatientService patientService,
                             AppointmentService appointmentService) {
        this.patientRepository = patientRepository;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
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

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal UserDetails user) {
        patientService.deletePatient(currentPatient(user).getPatientid());
    }

    @GetMapping("/doctors")
    public List<ApiDtos.DoctorView> searchDoctors(@RequestParam String name) {
        return patientService.searchDoctorsByName(name).stream().map(ApiDtos.DoctorView::from).toList();
    }

    @GetMapping("/appointments")
    @Transactional(readOnly = true)
    public List<ApiDtos.AppointmentView> appointments(@AuthenticationPrincipal UserDetails user) {
        Long patientId = currentPatient(user).getPatientid();
        return patientService.viewAppointmentsByPatient(patientId).stream()
                .map(ApiDtos.AppointmentView::from).toList();
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ApiDtos.AppointmentView createAppointment(@AuthenticationPrincipal UserDetails user,
                                                      @Valid @RequestBody AppointmentInput input) {
        Appointment appointment = new Appointment();
        Doctor doctor = new Doctor();
        doctor.setDoctorid(input.doctorId());
        appointment.setDoctor(doctor);
        Patient patient = new Patient();
        patient.setPatientid(currentPatient(user).getPatientid());
        appointment.setPatient(patient);
        appointment.setAppointmentTime(input.appointmentTime());
        appointment.setStatus(0);
        return ApiDtos.AppointmentView.from(appointmentService.createAppointment(appointment));
    }

    @PatchMapping("/appointments/{id}/cancel")
    @Transactional
    public ApiDtos.AppointmentView cancelAppointment(@AuthenticationPrincipal UserDetails user,
                                                       @PathVariable Long id) {
        Appointment appointment = ownedAppointment(user, id);
        appointmentService.cancelAppointment(id);
        appointment.setStatus(2);
        return ApiDtos.AppointmentView.from(appointment);
    }

    @PatchMapping("/appointments/{id}/reschedule")
    @Transactional
    public ApiDtos.AppointmentView rescheduleAppointment(@AuthenticationPrincipal UserDetails user,
                                                          @PathVariable Long id,
                                                          @RequestParam LocalDate date) {
        ownedAppointment(user, id);
        return ApiDtos.AppointmentView.from(appointmentService.rescheduleAppointment(id, date));
    }

    @GetMapping("/prescriptions")
    public List<ApiDtos.PrescriptionView> prescriptions(@AuthenticationPrincipal UserDetails user) {
        String patientId = currentPatient(user).getPatientid().toString();
        return patientService.viewPrescriptionsByPatient(patientId).stream()
                .map(ApiDtos.PrescriptionView::from).toList();
    }

    private Patient currentPatient(UserDetails user) {
        return patientRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient account not found."));
    }

    private Appointment ownedAppointment(UserDetails user, Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        if (!appointment.getPatient().getPatientid().equals(currentPatient(user).getPatientid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This appointment does not belong to you.");
        }
        return appointment;
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

    public record AppointmentInput(
            @NotNull Long doctorId,
            @NotNull @jakarta.validation.constraints.FutureOrPresent LocalDate appointmentTime) {
    }
}
