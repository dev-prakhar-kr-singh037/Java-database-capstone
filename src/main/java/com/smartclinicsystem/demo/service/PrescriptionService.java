package com.smartclinicsystem.demo.service;

import java.util.List;
import java.util.Optional;

import com.smartclinicsystem.demo.document.Prescription;

public interface PrescriptionService {

    Prescription createPrescription(Prescription prescription);

    Prescription updatePrescription(String prescriptionId, Prescription updatedPrescription);

    void deletePrescription(String prescriptionId);

    Optional<Prescription> getPrescriptionById(String prescriptionId);

    List<Prescription> getAllPrescriptions();

    List<Prescription> getPrescriptionsByPatient(String patientId);

    List<Prescription> getPrescriptionsByDoctor(String doctorId);

    List<Prescription> getPrescriptionsByAppointment(String appointmentId);

    boolean validatePrescriptionLinkage(String patientId, String doctorId, String appointmentId);
}
