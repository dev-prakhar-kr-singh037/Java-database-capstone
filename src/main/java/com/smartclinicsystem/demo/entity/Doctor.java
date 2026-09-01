package com.smartclinicsystem.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity
public class Doctor {
    
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Id
    private Long doctorid;

    @NotNull
    private String firstname;

    @NotNull
    private String lastname;

    @NotNull
    private String specialisation;

    @NotNull
    private int contact;

    @NotNull @Email
    private String email;

    private LocalDate createdat;

    public Doctor() {
    }

    public Doctor(Long doctorid, String firstname, String lastname, String specialisation, int contact, String email, LocalDate createdat) {
        this.doctorid = doctorid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.specialisation = specialisation;
        this.contact = contact;
        this.email = email;
        this.createdat = createdat;
    }

    public Long getDoctorid() {
        return doctorid;
    }

    public void setDoctorid(Long doctorid) {
        this.doctorid = doctorid;
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

    public String getSpecialisation() {
        return specialisation;
    }

    public void setSpecialisation(String specialisation) {
        this.specialisation = specialisation;
    }

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
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


}
