package com.smartclinicsystem.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartclinicsystem.demo.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    Optional<Appointment> findByAppointment_id(int appointmentId);

    List<Appointment> findByPatient_Patientid(Long patientId);

    List<Appointment> findByDoctor_Doctorid(Long doctorId);

    List<Appointment> findByDoctor_DoctoridAndAppointmentTime(Long doctorId, LocalDate appointmentTime);

    boolean existsByPatient_PatientidAndDoctor_DoctoridAndAppointmentTime(Long patientId, Long doctorId, LocalDate appointmentTime);
}