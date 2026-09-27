package com.smartclinicsystem.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.repository.PrescriptionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    public Prescription createPrescription(Prescription prescription) {
        if (prescription == null) {
            throw new IllegalArgumentException("Prescription cannot be null.");
        }
        if (prescription.getNotes() == null || prescription.getNotes().isBlank()) {
            throw new IllegalArgumentException("Prescription notes are required.");
        }
        if (prescription.getCreatedAt() == null) {
            prescription.setCreatedAt(LocalDate.now());
        }
        if (prescription.getIssuedDate() == null) {
            prescription.setIssuedDate(LocalDate.now());
        }
        return prescriptionRepository.save(prescription);
    }

    @Override
    public Prescription updatePrescription(String prescriptionId, Prescription updatedPrescription) {
        Prescription existingPrescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found: " + prescriptionId));

        if (updatedPrescription.getNotes() == null || updatedPrescription.getNotes().isBlank()) {
            throw new IllegalArgumentException("Prescription notes are required.");
        }

        existingPrescription.setPatientId(updatedPrescription.getPatientId());
        existingPrescription.setDoctorId(updatedPrescription.getDoctorId());
        existingPrescription.setAppointmentId(updatedPrescription.getAppointmentId());
        existingPrescription.setIssuedDate(updatedPrescription.getIssuedDate());
        existingPrescription.setNotes(updatedPrescription.getNotes());
        if (updatedPrescription.getCreatedAt() != null) {
            existingPrescription.setCreatedAt(updatedPrescription.getCreatedAt());
        }

        return prescriptionRepository.save(existingPrescription);
    }

    @Override
    public void deletePrescription(String prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found: " + prescriptionId));
        prescriptionRepository.delete(prescription);
    }

    @Override
    public Optional<Prescription> getPrescriptionById(String prescriptionId) {
        return prescriptionRepository.findById(prescriptionId);
    }

    @Override
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    @Override
    public List<Prescription> getPrescriptionsByPatient(String patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    @Override
    public List<Prescription> getPrescriptionsByDoctor(String doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Prescription> getPrescriptionsByAppointment(String appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public boolean validatePrescriptionLinkage(String patientId, String doctorId, String appointmentId) {
        return prescriptionRepository.findByPatientIdAndDoctorIdAndAppointmentId(patientId, doctorId, appointmentId)
                .stream()
                .findAny()
                .isPresent();
    }
}
