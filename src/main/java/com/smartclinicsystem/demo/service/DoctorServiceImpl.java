package com.smartclinicsystem.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.repository.AppointmentRepository;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.repository.PrescriptionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             PrescriptionRepository prescriptionRepository,
                             AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public Doctor updateDoctor(Long doctorid, Doctor updatedDoctor) {
        Doctor doctor = doctorRepository.findById(doctorid)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorid));
        doctor.setFirstname(updatedDoctor.getFirstname());
        doctor.setLastname(updatedDoctor.getLastname());
        doctor.setSpecialisation(updatedDoctor.getSpecialisation());
        doctor.setContact(updatedDoctor.getContact());
        doctor.setEmail(updatedDoctor.getEmail());

        return doctorRepository.save(doctor);
    }

    @Override
    public Prescription writePrescription(Prescription prescription) {
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
    public List<Prescription> viewPrescriptionsByDoctor(String doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> viewAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctor_Doctorid(doctorId);
    }
}
