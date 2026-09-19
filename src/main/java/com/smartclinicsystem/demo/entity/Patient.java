package com.smartclinicsystem.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity
public class Patient {
    
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Id
    private Long patientid;

    @NotNull
    private String firstname;

    @NotNull
    private String lastname;

    @NotNull
    private String dob;

    @NotNull
    private String gender;

    @NotNull
    private String phoneno;

    @NotNull @Email
    private String email;

    private LocalDate createdat;

    @NotNull
    private String address;
    
    public Patient() {
    }
    
    public Patient(Long patientid, String firstname, String lastname, String dob, String gender, String phoneno, String email, LocalDate createdat, String address) {
        this.patientid = patientid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dob = dob;
        this.gender = gender;
        this.phoneno = phoneno;
        this.email = email;
        this.createdat = createdat;
        this.address = address;
    }
    
    public Long getPatientid() {
        return patientid;
    }
    
    public void setPatientid(Long patientid) {
        this.patientid = patientid;
    }
    
    public String getFirstname() {
        return firstname;
    }
    
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    
    public String getLastname() {
        return lastname;
    }
    
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    public String getDob() {
        return dob;
    }
    
    public void setDob(String dob) {
        this.dob = dob;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPhoneno() {
        return phoneno;
    }
    
    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDate getCreatedat() {
        return createdat;
    }
    
    public void setCreatedat(LocalDate createdat) {
        this.createdat = createdat;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
}
