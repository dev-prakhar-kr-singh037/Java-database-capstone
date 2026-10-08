package com.smartclinicsystem.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.service.AppointmentService;
import com.smartclinicsystem.demo.service.DoctorService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public DoctorController(DoctorRepository doctorRepository,
                            DoctorService doctorService,
                            AppointmentService appointmentService) {
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
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

    @GetMapping("/appointments")
    @Transactional(readOnly = true)
    public List<ApiDtos.AppointmentView> appointments(@AuthenticationPrincipal UserDetails user) {
        Long doctorId = currentDoctor(user).getDoctorid();
        return doctorService.viewAppointmentsByDoctor(doctorId).stream()
                .map(ApiDtos.AppointmentView::from).toList();
    }

    @GetMapping("/prescriptions")
    public List<ApiDtos.PrescriptionView> prescriptions(@AuthenticationPrincipal UserDetails user) {
        String doctorId = currentDoctor(user).getDoctorid().toString();
        return doctorService.viewPrescriptionsByDoctor(doctorId).stream()
                .map(ApiDtos.PrescriptionView::from).toList();
    }

    @PostMapping("/prescriptions")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ApiDtos.PrescriptionView createPrescription(@AuthenticationPrincipal UserDetails user,
                                                        @Valid @RequestBody PrescriptionInput input) {
        Doctor doctor = currentDoctor(user);
        Appointment appointment = appointmentService.getAppointmentById(input.appointmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        if (!appointment.getDoctor().getDoctorid().equals(doctor.getDoctorid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only write prescriptions for your own appointments.");
        }

        Prescription prescription = new Prescription();
        prescription.setDoctorId(doctor.getDoctorid().toString());
        prescription.setPatientId(appointment.getPatient().getPatientid().toString());
        prescription.setAppointmentId(appointment.getAppointment_id().toString());
        prescription.setIssuedDate(input.issuedDate() == null ? LocalDate.now() : input.issuedDate());
        prescription.setNotes(input.notes());
        return ApiDtos.PrescriptionView.from(doctorService.writePrescription(prescription));
    }

    private Doctor currentDoctor(UserDetails user) {
        return doctorRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor account not found."));
    }

    public record DoctorProfileInput(
            @NotBlank String firstname,
            @NotBlank String lastname,
            @NotBlank String specialisation,
            @jakarta.validation.constraints.Positive int contact,
            String password) {
    }

    public record PrescriptionInput(
            @NotNull Long appointmentId,
            LocalDate issuedDate,
            @NotBlank String notes) {
    }
}
