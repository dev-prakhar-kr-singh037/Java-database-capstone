package com.smartclinicsystem.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;
import com.smartclinicsystem.demo.repository.AppointmentRepository;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.repository.PatientRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                 DoctorRepository doctorRepository,
                                 PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null.");
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getDoctorid() == null) {
            throw new IllegalArgumentException("Doctor is required for appointment creation.");
        }
        if (appointment.getPatient() == null || appointment.getPatient().getPatientid() == null) {
            throw new IllegalArgumentException("Patient is required for appointment creation.");
        }

        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getDoctorid())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + appointment.getDoctor().getDoctorid()));
        Patient patient = patientRepository.findById(appointment.getPatient().getPatientid())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + appointment.getPatient().getPatientid()));

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        if (appointment.getStatus() == null) {
            appointment.setStatus(0);
        }
        if (appointment.getAppointmentTime() == null) {
            appointment.setAppointmentTime(LocalDate.now());
        }

        if (appointmentRepository.existsByPatient_PatientidAndDoctor_DoctoridAndAppointmentTime(
                patient.getPatientid(), doctor.getDoctorid(), appointment.getAppointmentTime())) {
            throw new IllegalStateException("Appointment already exists for this patient/doctor/date.");
        }

        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment updateAppointment(Long appointmentId, Appointment updatedAppointment) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));

        if (updatedAppointment.getDoctor() != null && updatedAppointment.getDoctor().getDoctorid() != null) {
            Doctor doctor = doctorRepository.findById(updatedAppointment.getDoctor().getDoctorid())
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + updatedAppointment.getDoctor().getDoctorid()));
            existingAppointment.setDoctor(doctor);
        }

        if (updatedAppointment.getPatient() != null && updatedAppointment.getPatient().getPatientid() != null) {
            Patient patient = patientRepository.findById(updatedAppointment.getPatient().getPatientid())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + updatedAppointment.getPatient().getPatientid()));
            existingAppointment.setPatient(patient);
        }

        if (updatedAppointment.getAppointmentTime() != null) {
            existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        }
        if (updatedAppointment.getStatus() != null) {
            existingAppointment.setStatus(updatedAppointment.getStatus());
        }

        return appointmentRepository.save(existingAppointment);
    }

    @Override
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));
        appointment.setStatus(2);
        appointmentRepository.save(appointment);
    }

    @Override
    public Appointment rescheduleAppointment(Long appointmentId, LocalDate newAppointmentTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));
        appointment.setAppointmentTime(newAppointmentTime);
        appointment.setStatus(0);
        return appointmentRepository.save(appointment);
    }

    @Override
    public Optional<Appointment> getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatient_Patientid(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctor_Doctorid(doctorId);
    }

    @Override
    public boolean hasConflict(Long patientId, Long doctorId, LocalDate appointmentTime) {
        return appointmentRepository.existsByPatient_PatientidAndDoctor_DoctoridAndAppointmentTime(
                patientId, doctorId, appointmentTime);
    }
}
