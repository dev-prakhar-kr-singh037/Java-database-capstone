package com.smartclinicsystem.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.smartclinicsystem.demo.document.Prescription;

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    Optional<Prescription> findByPrescription_id(String prescriptionId);

    List<Prescription> findByPatientId(String patientId);

    List<Prescription> findByDoctorId(String doctorId);

    List<Prescription> findByAppointmentId(String appointmentId);

    List<Prescription> findByPatientIdAndDoctorIdAndAppointmentId(String patientId, String doctorId, String appointmentId);
}
