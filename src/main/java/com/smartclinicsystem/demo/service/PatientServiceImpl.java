package com.smartclinicsystem.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.AppointmentRepository;
import com.smartclinicsystem.demo.repository.PatientRepository;
import com.smartclinicsystem.demo.repository.PrescriptionRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientServiceImpl(PatientRepository patientRepository,
                              PrescriptionRepository prescriptionRepository,
                              AppointmentRepository appointmentRepository,
                              PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
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
    public List<Prescription> viewPrescriptionsByPatient(String patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> viewAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatient_Patientid(patientId);
    }
}
