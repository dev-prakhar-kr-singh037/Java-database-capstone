package com.smartclinicsystem.demo.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.AdminRepository;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.repository.PatientRepository;
import com.smartclinicsystem.demo.service.AdminService;
import com.smartclinicsystem.demo.service.DoctorService;
import com.smartclinicsystem.demo.service.PatientService;

@Controller
public class AuthController {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AdminService adminService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public AuthController(AdminRepository adminRepository,
                          DoctorRepository doctorRepository,
                          PatientRepository patientRepository,
                          AdminService adminService,
                          DoctorService doctorService,
                          PatientService patientService) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.adminService = adminService;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    @GetMapping("/login")
    public String login() {
        return "loginpage";
    }

    @InitBinder("doctor")
    public void restrictDoctorRegistrationFields(WebDataBinder binder) {
        binder.setAllowedFields("firstname", "lastname", "password", "specialisation", "contact", "email");
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user,
                            @RequestParam(required = false) String doctorName,
                            Model model) {
        String role = user.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElseThrow(() -> new AccessDeniedException("No role is assigned to this account."));

        model.addAttribute("username", user.getUsername());
        return switch (role) {
            case "ADMIN" -> {
                model.addAttribute("admin", adminRepository.findByUsername(user.getUsername())
                        .orElseThrow(() -> new AccessDeniedException("Admin account is no longer available.")));
                model.addAttribute("doctors", adminService.viewAllDoctors());
                model.addAttribute("patients", adminService.viewAllPatients());
                model.addAttribute("appointments", adminService.viewAllAppointments());
                model.addAttribute("prescriptions", adminService.viewAllPrescriptions());
                model.addAttribute("report", adminService.generateSystemReport());
                yield "admindashboard";
            }
            case "DOCTOR" -> {
                Doctor doctor = doctorRepository.findByEmail(user.getUsername())
                        .orElseThrow(() -> new AccessDeniedException("Doctor account is no longer available."));
                model.addAttribute("doctor", doctor);
                model.addAttribute("appointments", doctorService.viewAppointmentsByDoctor(doctor.getDoctorid()));
                model.addAttribute("prescriptions",
                        doctorService.viewPrescriptionsByDoctor(doctor.getDoctorid().toString()));
                yield "doctordashboard";
            }
            case "PATIENT" -> {
                Patient patient = patientRepository.findByEmail(user.getUsername())
                        .orElseThrow(() -> new AccessDeniedException("Patient account is no longer available."));
                model.addAttribute("patient", patient);
                model.addAttribute("appointments", patientService.viewAppointmentsByPatient(patient.getPatientid()));
                model.addAttribute("prescriptions",
                        patientService.viewPrescriptionsByPatient(patient.getPatientid().toString()));
                model.addAttribute("doctorSearchName", doctorName == null ? "" : doctorName);
                model.addAttribute("doctors", doctorName == null || doctorName.isBlank()
                        ? List.of()
                        : patientService.searchDoctorsByName(doctorName));
                yield "patientdashboard";
            }
            default -> throw new AccessDeniedException("This account does not have a clinic dashboard.");
        };
    }

    @PostMapping("/admin/doctors")
    public String createDoctor(@Valid @ModelAttribute Doctor doctor,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (doctor.getFirstname() == null || doctor.getFirstname().isBlank()
                || doctor.getLastname() == null || doctor.getLastname().isBlank()
                || doctor.getPassword() == null || doctor.getPassword().isBlank()
                || doctor.getSpecialisation() == null || doctor.getSpecialisation().isBlank()
                || doctor.getEmail() == null || doctor.getEmail().isBlank()
                || doctor.getContact() <= 0) {
            bindingResult.reject("doctor.required", "Complete all required doctor details.");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("doctorFormError",
                    bindingResult.getGlobalError() == null
                            ? "Enter valid doctor details and try again."
                            : bindingResult.getGlobalError().getDefaultMessage());
            return "redirect:/dashboard";
        }
        if (doctorRepository.existsByEmail(doctor.getEmail().trim())) {
            redirectAttributes.addFlashAttribute("doctorFormError",
                    "A doctor account with this email already exists.");
            return "redirect:/dashboard";
        }

        doctor.setFirstname(doctor.getFirstname().trim());
        doctor.setLastname(doctor.getLastname().trim());
        doctor.setEmail(doctor.getEmail().trim());
        doctor.setSpecialisation(doctor.getSpecialisation().trim());
        adminService.createDoctor(doctor);
        redirectAttributes.addFlashAttribute("doctorCreated",
                "Doctor account created successfully.");
        return "redirect:/dashboard";
    }
}
