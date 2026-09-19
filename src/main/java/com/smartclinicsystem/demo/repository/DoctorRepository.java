package com.smartclinicsystem.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.smartclinicsystem.demo.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByDoctorid(Long doctorid);

    @Modifying
    @Transactional
    @Query("UPDATE Doctor d SET d.firstname = :firstname, d.lastname = :lastname, " +
            "d.specialisation = :specialisation, d.contact = :contact, d.email = :email WHERE d.doctorid = :doctorid")
    int updateDoctorDetails(@Param("doctorid") Long doctorid,
                           @Param("firstname") String firstname,
                           @Param("lastname") String lastname,
                           @Param("specialisation") String specialisation,
                           @Param("contact") int contact,
                           @Param("email") String email);
}