package com.smartclinicsystem.demo.service;

import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.repository.DoctorRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;

public class DoctorServiceImpl implements DoctorService{

    private final DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository){
        this.doctorRepository=doctorRepository;
    }

    @Override
    public Doctor createDoctor(Doctor doctor){
        if (doctor.getCreatedat() == null) {
            doctor.setCreatedat(LocalDate.now());
        }
        return doctorRepository.save(doctor);
    
    }

    @Override
    public List<Doctor> getAllDoctors(){
        return doctorRepository.findAll();
    }

    @Override
	public Optional<Doctor> getDoctorById(Long doctorId){
        return doctorRepository.findById(doctorId);
    }

    @Override 
	public Doctor updateDoctor(Long doctorid, Doctor updatedDoctor){
        Doctor doctor=doctorRepository.findById(doctorid)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: "+doctorid));
        doctor.setFirstname(updatedDoctor.getFirstname());
        doctor.setLastname(updatedDoctor.getLastname());
        doctor.setSpecialisation(updatedDoctor.getSpecialisation());
        doctor.setContact(updatedDoctor.getContact());
        doctor.setEmail(updatedDoctor.getEmail());

        return doctorRepository.save(doctor);
    }
        
    
    @Override
	public void deleteDoctor(Long doctorId){
        Doctor doctor=doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId));
        doctorRepository.delete(doctor);
    }
}

    

