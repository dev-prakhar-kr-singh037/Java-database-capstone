package com.smartclinicsystem.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.PatientRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Patient createPatient(Patient patient) {
        if (patient.getCreatedat() == null) {
            patient.setCreatedat(LocalDate.now());
        }
        return patientRepository.save(patient);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Optional<Patient> getPatientById(Long patientId) {
        return patientRepository.findById(patientId);
    }

    @Override
    public Patient updatePatient(Long patientId, Patient updatedPatient) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId));

        patient.setFirstname(updatedPatient.getFirstname());
        patient.setLastname(updatedPatient.getLastname());
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
}