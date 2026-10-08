package com.smartclinicsystem.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.repository.PatientRepository;
import com.smartclinicsystem.demo.service.AdminService;
import com.smartclinicsystem.demo.service.AppointmentService;
import com.smartclinicsystem.demo.service.DoctorService;
import com.smartclinicsystem.demo.service.PatientService;
import com.smartclinicsystem.demo.service.PrescriptionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final AdminService adminService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public PrescriptionController(AdminService adminService,
                                  AppointmentService appointmentService,
                                  DoctorService doctorService,
                                  PatientService patientService,
                                  PrescriptionService prescriptionService,
                                  DoctorRepository doctorRepository,
                                  PatientRepository patientRepository) {
        this.adminService = adminService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping
    public List<ApiDtos.PrescriptionView> list(@AuthenticationPrincipal UserDetails user) {
        List<Prescription> prescriptions = switch (role(user)) {
            case "ADMIN" -> adminService.viewAllPrescriptions();
            case "DOCTOR" -> doctorService.viewPrescriptionsByDoctor(currentDoctor(user).getDoctorid().toString());
            case "PATIENT" -> patientService.viewPrescriptionsByPatient(currentPatientId(user));
            default -> throw forbidden("This role cannot view prescriptions.");
        };
        return prescriptions.stream().map(ApiDtos.PrescriptionView::from).toList();
    }

    @GetMapping("/{id}")
    public ApiDtos.PrescriptionView get(@AuthenticationPrincipal UserDetails user, @PathVariable String id) {
        Prescription prescription = prescriptionService.getPrescriptionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found."));
        authorizeRead(user, prescription);
        return ApiDtos.PrescriptionView.from(prescription);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ApiDtos.PrescriptionView create(@AuthenticationPrincipal UserDetails user,
                                           @Valid @RequestBody PrescriptionInput input) {
        if (!"DOCTOR".equals(role(user))) {
            throw forbidden("Only doctors can create prescriptions.");
        }
        Doctor doctor = currentDoctor(user);
        Appointment appointment = appointmentService.getAppointmentById(input.appointmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        if (appointment.getDoctor() == null
                || !appointment.getDoctor().getDoctorid().equals(doctor.getDoctorid())) {
            throw forbidden("You can only write prescriptions for your own appointments.");
        }

        Prescription prescription = new Prescription();
        prescription.setDoctorId(doctor.getDoctorid().toString());
        prescription.setPatientId(appointment.getPatient().getPatientid().toString());
        prescription.setAppointmentId(appointment.getAppointment_id().toString());
        prescription.setIssuedDate(input.issuedDate() == null ? LocalDate.now() : input.issuedDate());
        prescription.setNotes(input.notes());
        return ApiDtos.PrescriptionView.from(doctorService.writePrescription(prescription));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails user, @PathVariable String id) {
        requireAdmin(user);
        adminService.deletePrescription(id);
    }

    @PostMapping("/audit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void audit(@AuthenticationPrincipal UserDetails user) {
        requireAdmin(user);
        adminService.auditPrescriptionCompliance();
    }

    private void authorizeRead(UserDetails user, Prescription prescription) {
        switch (role(user)) {
            case "ADMIN" -> {
            }
            case "DOCTOR" -> {
                if (!currentDoctor(user).getDoctorid().toString().equals(prescription.getDoctorId())) {
                    throw forbidden("This prescription does not belong to you.");
                }
            }
            case "PATIENT" -> {
                if (!currentPatientId(user).equals(prescription.getPatientId())) {
                    throw forbidden("This prescription does not belong to you.");
                }
            }
            default -> throw forbidden("This role cannot view prescriptions.");
        }
    }

    private String currentPatientId(UserDetails user) {
        return patientRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient account not found."))
                .getPatientid().toString();
    }

    private Doctor currentDoctor(UserDetails user) {
        return doctorRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor account not found."));
    }

    private String role(UserDetails user) {
        return user.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .filter(candidate -> List.of("ADMIN", "DOCTOR", "PATIENT").contains(candidate))
                .findFirst()
                .orElseThrow(() -> forbidden("A clinic role is required."));
    }

    private void requireAdmin(UserDetails user) {
        if (!"ADMIN".equals(role(user))) {
            throw forbidden("Admin access is required.");
        }
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    public record PrescriptionInput(
            @NotNull Long appointmentId,
            LocalDate issuedDate,
            @NotBlank String notes) {
    }
}
