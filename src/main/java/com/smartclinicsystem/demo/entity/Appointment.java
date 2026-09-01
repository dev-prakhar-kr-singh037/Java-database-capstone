package com.smartclinicsystem.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int appointment_id;

    // Foreign key to Doctor
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Many appointments can belong to one patient
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patients patient;

    // Appointment date & time
    @NotNull
    @FutureOrPresent   // ensures appointment is now or in future
    @Column(name = "appointment_time")
    private LocalDate appointmentTime;

    // Status: 0 = Scheduled, 1 = Completed, 2 = Cancelled
    @NotNull
    @Column(name = "status")
    private Integer status;

    public Appointment() {
    }

    public Appointment(int appointment_id, Doctor doctor, Patients patient, LocalDate appointmentTime, Integer status) {
        this.appointment_id = appointment_id;
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patients getPatient() {
        return patient;
    }

    public void setPatient(Patients patient) {
        this.patient = patient;
    }

    public int getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(int appointment_id) {
        this.appointment_id = appointment_id;
    }

    public LocalDate getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDate appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
