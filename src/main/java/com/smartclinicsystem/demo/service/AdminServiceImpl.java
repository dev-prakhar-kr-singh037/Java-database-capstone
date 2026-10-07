package com.smartclinicsystem.demo.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Admin;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.AdminRepository;
import com.smartclinicsystem.demo.repository.AppointmentRepository;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.repository.PatientRepository;
import com.smartclinicsystem.demo.repository.PrescriptionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminRepository adminRepository,
                           AppointmentRepository appointmentRepository,
                           PrescriptionRepository prescriptionRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Admin createAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(LocalDate.now());
        }
        return adminRepository.save(admin);
    }

    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Optional<Admin> getAdminById(Long adminId) {
        return adminRepository.findById(adminId);
    }

    @Override
    public Admin updateAdmin(Long adminId, Admin updatedAdmin) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found: " + adminId));

        admin.setUsername(updatedAdmin.getUsername());
        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(updatedAdmin.getPassword()));
        }
        admin.setFirstname(updatedAdmin.getFirstname());
        admin.setLastname(updatedAdmin.getLastname());
        admin.setEmail(updatedAdmin.getEmail());
        admin.setContact(updatedAdmin.getContact());

        return adminRepository.save(admin);
    }

    @Override
    public void deleteAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found: " + adminId));
        adminRepository.delete(admin);
    }

    // Doctor management (admin responsibility)
    @Override
    public Doctor createDoctor(Doctor doctor) {
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        if (doctor.getCreatedat() == null) {
            doctor.setCreatedat(LocalDate.now());
        }
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctor(Long doctorId, Doctor updatedDoctor) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId));

        doctor.setFirstname(updatedDoctor.getFirstname());
        doctor.setLastname(updatedDoctor.getLastname());
        if (updatedDoctor.getPassword() != null && !updatedDoctor.getPassword().isBlank()) {
            doctor.setPassword(passwordEncoder.encode(updatedDoctor.getPassword()));
        }
        doctor.setSpecialisation(updatedDoctor.getSpecialisation());
        doctor.setContact(updatedDoctor.getContact());
        doctor.setEmail(updatedDoctor.getEmail());

        return doctorRepository.save(doctor);
    }

    @Override
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId));
        doctorRepository.delete(doctor);
    }

    @Override
    public List<Doctor> viewAllDoctors() {
        return doctorRepository.findAll();
    }

    // Patient management (admin responsibility)
    @Override
    public Patient createPatient(Patient patient) {
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        if (patient.getCreatedat() == null) {
            patient.setCreatedat(LocalDate.now());
        }
        return patientRepository.save(patient);
    }

    @Override
    public Patient updatePatient(Long patientId, Patient updatedPatient) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId));

        patient.setFirstname(updatedPatient.getFirstname());
        patient.setLastname(updatedPatient.getLastname());
        if (updatedPatient.getPassword() != null && !updatedPatient.getPassword().isBlank()) {
            patient.setPassword(passwordEncoder.encode(updatedPatient.getPassword()));
        }
        patient.setDob(updatedPatient.getDob());
        patient.setGender(updatedPatient.getGender());
        patient.setPhoneno(updatedPatient.getPhoneno());
        patient.setEmail(updatedPatient.getEmail());
        patient.setAddress(updatedPatient.getAddress());

        return patientRepository.save(patient);
    }

    @Override
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId));
        patientRepository.delete(patient);
    }

    @Override
    public List<Patient> viewAllPatients() {
        return patientRepository.findAll();
    }

    // Appointment oversight
    @Override
    public List<Appointment> viewAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));
        appointment.setStatus(2);
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment rescheduleAppointment(Long appointmentId, LocalDate newAppointmentTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));
        appointment.setAppointmentTime(newAppointmentTime);
        appointment.setStatus(0);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void resolveAppointmentConflict(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));

        if (appointment.getDoctor() == null || appointment.getDoctor().getDoctorid() == null) {
            throw new IllegalStateException("Appointment is missing a doctor reference.");
        }

        // Find other appointments with same doctor & time
        List<Appointment> conflicts = appointmentRepository
                .findByDoctor_DoctoridAndAppointmentTime(appointment.getDoctor().getDoctorid(), appointment.getAppointmentTime());

        if (conflicts.size() > 1) {
            // Example strategy: cancel all but the first
            for (int i = 1; i < conflicts.size(); i++) {
                conflicts.get(i).setStatus(2); // cancelled
                appointmentRepository.save(conflicts.get(i));
            }
        }
    }

    @Override
    public List<Appointment> findByDoctorIdAndAppointmentTime(Long doctorId, LocalDate appointmentTime) {
        return appointmentRepository.findByDoctor_DoctoridAndAppointmentTime(doctorId, appointmentTime);
    }


    // Prescription oversight
    @Override
    public List<Prescription> viewAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    @Override
    public void deletePrescription(String prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found: " + prescriptionId));
        prescriptionRepository.delete(prescription);
    }

    @Override
    public void auditPrescriptionCompliance() {
        List<Prescription> prescriptions = prescriptionRepository.findAll();
        for (Prescription prescription : prescriptions) {
            if (prescription.getDoctorId() == null || prescription.getPatientId() == null || prescription.getAppointmentId() == null) {
                throw new IllegalStateException("Prescription compliance issue detected for id: " + prescription.getPrescription_id());
            }
        }
    }

    @Override
    public Map<String, Object> generateSystemReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Appointment> appointments = appointmentRepository.findAll();
        List<Prescription> prescriptions = prescriptionRepository.findAll();

        Map<String, Long> doctorAppointmentCounts = new HashMap<>();
        for (Appointment appointment : appointments) {
            Doctor doctor = appointment.getDoctor();
            if (doctor != null) {
                doctorAppointmentCounts.put(doctor.getFirstname() + " " + doctor.getLastname(),
                        doctorAppointmentCounts.getOrDefault(doctor.getFirstname() + " " + doctor.getLastname(), 0L) + 1L);
            }
        }

        String busiestDoctor = "N/A";
        long maxAppointments = 0L;
        for (Map.Entry<String, Long> entry : doctorAppointmentCounts.entrySet()) {
            if (entry.getValue() > maxAppointments) {
                busiestDoctor = entry.getKey();
                maxAppointments = entry.getValue();
            }
        }

        Map<String, Long> prescriptionTrends = new HashMap<>();
        for (Prescription prescription : prescriptions) {
            String key = prescription.getIssuedDate() == null ? "unknown" : prescription.getIssuedDate().toString();
            prescriptionTrends.put(key, prescriptionTrends.getOrDefault(key, 0L) + 1L);
        }

        report.put("totalAppointments", appointments.size());
        report.put("totalPrescriptions", prescriptions.size());
        report.put("busiestDoctor", busiestDoctor);
        report.put("busiestDoctorAppointmentCount", maxAppointments);
        report.put("prescriptionTrends", prescriptionTrends);

        return report;
    }

    @Override
    public void manageSecurityRules(String ruleType, String ruleValue) {
        if (ruleType == null || ruleType.isBlank()) {
            throw new IllegalArgumentException("Rule type is required.");
        }
        if (ruleValue == null || ruleValue.isBlank()) {
            throw new IllegalArgumentException("Rule value is required.");
        }
    }

    @Override
    public Admin createOrUpdateUser(Admin admin) {
        if (admin.getAdminId() != null && adminRepository.existsById(admin.getAdminId())) {
            return updateAdmin(admin.getAdminId(), admin);
        }
        return createAdmin(admin);
    }
}
