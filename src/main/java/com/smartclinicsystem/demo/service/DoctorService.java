package com.smartclinicsystem.demo.service;

import java.util.List;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;

public interface DoctorService {

    // Doctor profile updates remain here; creation/deletion/listing is admin responsibility
    Doctor updateDoctor(Long doctorid, Doctor updatedDoctor);

    // Doctor writes prescriptions and can view their own prescriptions
    Prescription writePrescription(Prescription prescription);

    List<Prescription> viewPrescriptionsByDoctor(String doctorId);

    // Doctor can view appointments for themselves
    List<Appointment> viewAppointmentsByDoctor(Long doctorId);
}
