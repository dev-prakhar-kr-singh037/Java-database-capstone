package com.smartclinicsystem.demo.controller;

import java.time.LocalDate;

import com.smartclinicsystem.demo.document.Prescription;
import com.smartclinicsystem.demo.entity.Admin;
import com.smartclinicsystem.demo.entity.Appointment;
import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record AdminView(Long id, String username, String firstName, String lastName,
                            String email, int contact, LocalDate createdAt) {
        public static AdminView from(Admin admin) {
            return new AdminView(admin.getAdminId(), admin.getUsername(), admin.getFirstname(),
                    admin.getLastname(), admin.getEmail(), admin.getContact(), admin.getCreatedAt());
        }
    }

    public record DoctorView(Long id, String firstName, String lastName, String specialisation,
                             int contact, String email, LocalDate createdAt) {
        public static DoctorView from(Doctor doctor) {
            return new DoctorView(doctor.getDoctorid(), doctor.getFirstname(), doctor.getLastname(),
                    doctor.getSpecialisation(), doctor.getContact(), doctor.getEmail(), doctor.getCreatedat());
        }
    }

    public record PatientView(Long id, String firstName, String lastName, String dateOfBirth,
                              String gender, String phone, String email, String address, LocalDate createdAt) {
        public static PatientView from(Patient patient) {
            return new PatientView(patient.getPatientid(), patient.getFirstname(), patient.getLastname(),
                    patient.getDob(), patient.getGender(), patient.getPhoneno(), patient.getEmail(),
                    patient.getAddress(), patient.getCreatedat());
        }
    }

    public record AppointmentView(Long id, Long doctorId, String doctorName, Long patientId, String patientName,
                                  LocalDate appointmentTime, Integer status) {
        public static AppointmentView from(Appointment appointment) {
            Doctor doctor = appointment.getDoctor();
            Patient patient = appointment.getPatient();
            return new AppointmentView(appointment.getAppointment_id(),
                    doctor == null ? null : doctor.getDoctorid(),
                    doctor == null ? null : doctor.getFirstname() + " " + doctor.getLastname(),
                    patient == null ? null : patient.getPatientid(),
                    patient == null ? null : patient.getFirstname() + " " + patient.getLastname(),
                    appointment.getAppointmentTime(), appointment.getStatus());
        }
    }

    public record PrescriptionView(String id, String patientId, String doctorId, String appointmentId,
                                    LocalDate issuedDate, String notes, LocalDate createdAt) {
        public static PrescriptionView from(Prescription prescription) {
            return new PrescriptionView(prescription.getPrescription_id(), prescription.getPatientId(),
                    prescription.getDoctorId(), prescription.getAppointmentId(), prescription.getIssuedDate(),
                    prescription.getNotes(), prescription.getCreatedAt());
        }
    }
}
