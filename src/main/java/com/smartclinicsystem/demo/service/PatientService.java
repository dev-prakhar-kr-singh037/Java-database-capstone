package com.smartclinicsystem.demo.service;

import java.util.List;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Patient;

public interface PatientService {

    // Patient account update & self-delete remain here
    Patient updatePatient(Long patientId, Patient updatedPatient);

    void deletePatient(Long patientId);

    // Patient can view their own prescriptions (read-only)
    List<Prescription> viewPrescriptionsByPatient(String patientId);

    // Patient can view their appointments
    List<Appointment> viewAppointmentsByPatient(Long patientId);
}
