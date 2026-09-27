package com.smartclinicsystem.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Admin;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;

public interface AdminService {

    // Admin account management
    Admin createAdmin(Admin admin);

    List<Admin> getAllAdmins();

    Optional<Admin> getAdminById(Long adminId);

    Admin updateAdmin(Long adminId, Admin updatedAdmin);

    void deleteAdmin(Long adminId);

    // Doctor management (admin only)
    Doctor createDoctor(Doctor doctor);

    Doctor updateDoctor(Long doctorId, Doctor updatedDoctor);

    void deleteDoctor(Long doctorId);

    List<Doctor> viewAllDoctors();

    // Patient management (admin only)
    Patient createPatient(Patient patient);

    Patient updatePatient(Long patientId, Patient updatedPatient);

    void deletePatient(Long patientId);

    List<Patient> viewAllPatients();

    // Appointment oversight
    List<Appointment> viewAllAppointments();

    Appointment cancelAppointment(Long appointmentId);

    Appointment rescheduleAppointment(Long appointmentId, java.time.LocalDate newAppointmentTime);

    void resolveAppointmentConflict(Long appointmentId);

    // Prescription oversight
    List<Prescription> viewAllPrescriptions();

    void deletePrescription(String prescriptionId);

    void auditPrescriptionCompliance();

    Map<String, Object> generateSystemReport();

    void manageSecurityRules(String ruleType, String ruleValue);

    // Convenience: create or update users (doctors/patients) via admin UI
    Admin createOrUpdateUser(Admin admin);

    List<Appointment> findByDoctorIdAndAppointmentTime(Long doctorId, LocalDate appointmentTime);

}
