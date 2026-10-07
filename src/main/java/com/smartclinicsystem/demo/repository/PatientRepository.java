package com.smartclinicsystem.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.smartclinicsystem.demo.entity.Doctor;
import com.smartclinicsystem.demo.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientid(Long patientid);

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByEmailAndPassword(String email, String password);

        @Query("SELECT d FROM Patient p JOIN p.doctors d WHERE p.patientid = :patientid")
        List<Doctor> findAllDoctorsByPatientid(@Param("patientid") Long patientid);

    @Modifying
    @Transactional
    @Query("UPDATE Patient p SET p.firstname = :firstname, p.lastname = :lastname, p.password = :password, p.gender = :gender, " +
            "p.phoneno = :phoneno, p.email = :email, p.address = :address WHERE p.patientid = :patientid")
    int updatePatientDetails(@Param("patientid") Long patientid,
                            @Param("firstname") String firstname,
                            @Param("lastname") String lastname,
                            @Param("password") String password,
                            @Param("gender") String gender,
                            @Param("phoneno") String phoneno,
                            @Param("email") String email,
                            @Param("address") String address);
}