package com.smartclinicsystem.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.smartclinicsystem.demo.entity.Appointment;

public interface AppointmentService {

    Appointment createAppointment(Appointment appointment);

    Appointment updateAppointment(Long appointmentId, Appointment updatedAppointment);

    void cancelAppointment(Long appointmentId);

    Appointment rescheduleAppointment(Long appointmentId, LocalDate newAppointmentTime);

    Optional<Appointment> getAppointmentById(Long appointmentId);

    List<Appointment> getAllAppointments();

    List<Appointment> getAppointmentsByPatient(Long patientId);

    List<Appointment> getAppointmentsByDoctor(Long doctorId);

    boolean hasConflict(Long patientId, Long doctorId, LocalDate appointmentTime);
}
