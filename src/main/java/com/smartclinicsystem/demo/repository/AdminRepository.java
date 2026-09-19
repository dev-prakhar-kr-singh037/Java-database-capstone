package com.smartclinicsystem.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.smartclinicsystem.demo.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByUsernameAndPassword(String username, String password);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Admin a SET a.username = :username, a.password = :password, a.firstname = :firstname, " +
            "a.lastname = :lastname, a.email = :email, a.contact = :contact WHERE a.adminId = :adminId")
    int updateAdminDetails(@Param("adminId") Long adminId,
                          @Param("username") String username,
                          @Param("password") String password,
                          @Param("firstname") String firstname,
                          @Param("lastname") String lastname,
                          @Param("email") String email,
                          @Param("contact") int contact);
}
