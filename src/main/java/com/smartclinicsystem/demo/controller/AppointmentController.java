package com.smartclinicsystem.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.repository.PatientRepository;
import com.smartclinicsystem.demo.service.AdminService;
import com.smartclinicsystem.demo.service.AppointmentService;
import com.smartclinicsystem.demo.service.DoctorService;
import com.smartclinicsystem.demo.service.PatientService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AdminService adminService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentController(AdminService adminService,
                                 AppointmentService appointmentService,
                                 DoctorService doctorService,
                                 PatientService patientService,
                                 DoctorRepository doctorRepository,
                                 PatientRepository patientRepository) {
        this.adminService = adminService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ApiDtos.AppointmentView> list(@AuthenticationPrincipal UserDetails user) {
        List<Appointment> appointments = switch (role(user)) {
            case "ADMIN" -> adminService.viewAllAppointments();
            case "DOCTOR" -> doctorService.viewAppointmentsByDoctor(currentDoctor(user).getDoctorid());
            case "PATIENT" -> patientService.viewAppointmentsByPatient(currentPatient(user).getPatientid());
            default -> throw forbidden("This role cannot view appointments.");
        };
        return appointments.stream().map(ApiDtos.AppointmentView::from).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ApiDtos.AppointmentView get(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        Appointment appointment = findAppointment(id);
        authorizeRead(user, appointment);
        return ApiDtos.AppointmentView.from(appointment);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ApiDtos.AppointmentView create(@AuthenticationPrincipal UserDetails user,
                                          @Valid @RequestBody AppointmentInput input) {
        if (!"PATIENT".equals(role(user))) {
            throw forbidden("Only patients can create appointments.");
        }
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

    @PatchMapping("/{id}/cancel")
    @Transactional
    public ApiDtos.AppointmentView cancel(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        String role = role(user);
        if ("PATIENT".equals(role)) {
            authorizePatient(user, findAppointment(id));
            appointmentService.cancelAppointment(id);
        } else if ("ADMIN".equals(role)) {
            adminService.cancelAppointment(id);
        } else {
            throw forbidden("Only the appointment's patient or an admin can cancel it.");
        }
        return ApiDtos.AppointmentView.from(findAppointment(id));
    }

    @PatchMapping("/{id}/reschedule")
    @Transactional
    public ApiDtos.AppointmentView reschedule(@AuthenticationPrincipal UserDetails user,
                                              @PathVariable Long id,
                                              @RequestParam LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment date must be today or later.");
        }
        String role = role(user);
        if ("PATIENT".equals(role)) {
            authorizePatient(user, findAppointment(id));
            appointmentService.rescheduleAppointment(id, date);
        } else if ("ADMIN".equals(role)) {
            adminService.rescheduleAppointment(id, date);
        } else {
            throw forbidden("Only the appointment's patient or an admin can reschedule it.");
        }
        return ApiDtos.AppointmentView.from(findAppointment(id));
    }

    @PostMapping("/{id}/resolve-conflict")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resolveConflict(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        requireAdmin(user);
        adminService.resolveAppointmentConflict(id);
    }

    private void authorizeRead(UserDetails user, Appointment appointment) {
        switch (role(user)) {
            case "ADMIN" -> {
            }
            case "DOCTOR" -> {
                if (appointment.getDoctor() == null
                        || !appointment.getDoctor().getDoctorid().equals(currentDoctor(user).getDoctorid())) {
                    throw forbidden("This appointment does not belong to you.");
                }
            }
            case "PATIENT" -> authorizePatient(user, appointment);
            default -> throw forbidden("This role cannot view appointments.");
        }
    }

    private void authorizePatient(UserDetails user, Appointment appointment) {
        if (appointment.getPatient() == null
                || !appointment.getPatient().getPatientid().equals(currentPatient(user).getPatientid())) {
            throw forbidden("This appointment does not belong to you.");
        }
    }

    private Appointment findAppointment(Long id) {
        return appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
    }

    private Doctor currentDoctor(UserDetails user) {
        return doctorRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor account not found."));
    }

    private Patient currentPatient(UserDetails user) {
        return patientRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient account not found."));
    }

    private void requireAdmin(UserDetails user) {
        if (!"ADMIN".equals(role(user))) {
            throw forbidden("Admin access is required.");
        }
    }

    private String role(UserDetails user) {
        return user.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .filter(candidate -> List.of("ADMIN", "DOCTOR", "PATIENT").contains(candidate))
                .findFirst()
                .orElseThrow(() -> forbidden("A clinic role is required."));
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    public record AppointmentInput(
            @NotNull Long doctorId,
            @NotNull @FutureOrPresent LocalDate appointmentTime) {
    }
}
