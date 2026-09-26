package com.smartclinicsystem.demo.service;

import java.util.List;
import java.util.Optional;

import com.smartclinicsystem.demo.entity.Doctor;

public interface DoctorService {
    Doctor createDoctor(Doctor doctor);

	List<Doctor> getAllDoctors();

	Optional<Doctor> getDoctorById(Long doctorId);

	Doctor updateDoctor(Long doctorid, Doctor updatedDoctor);

	void deleteDoctor(Long doctorid);
}
