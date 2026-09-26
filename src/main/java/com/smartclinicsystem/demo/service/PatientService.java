package com.smartclinicsystem.demo.service;

import java.util.List;
import java.util.Optional;

import com.smartclinicsystem.demo.entity.Patient;

public interface PatientService {

	Patient createPatient(Patient patient);

	List<Patient> getAllPatients();

	Optional<Patient> getPatientById(Long patientId);

	Patient updatePatient(Long patientId, Patient updatedPatient);

	void deletePatient(Long patientId);
}
