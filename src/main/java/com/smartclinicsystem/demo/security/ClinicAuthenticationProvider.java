/* looks up the account in the matching repository and assigns the corresponding role:
 ROLE_ADMIN, ROLE_DOCTOR, or ROLE_PATIENT. The selection does not bypass password verification. */
package com.smartclinicsystem.demo.security;

import java.util.function.Consumer;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.smartclinicsystem.demo.repository.AdminRepository;
import com.smartclinicsystem.demo.repository.DoctorRepository;
import com.smartclinicsystem.demo.repository.PatientRepository;

@Component
public class ClinicAuthenticationProvider implements AuthenticationProvider {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClinicAuthenticationProvider(AdminRepository adminRepository,
                                        DoctorRepository doctorRepository,
                                        PatientRepository patientRepository,
                                        PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication.getDetails() instanceof LoginAuthenticationDetails details)) {
            throw new BadCredentialsException("Invalid login request.");
        }

        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        UserDetails user = switch (details.getUserType() == null ? "" : details.getUserType()) {
            case "ADMIN" -> adminRepository.findByUsername(username)
                    .map(admin -> authenticateAccount(admin.getUsername(), admin.getPassword(), password, "ADMIN",
                            newPassword -> {
                                admin.setPassword(newPassword);
                                adminRepository.save(admin);
                            }))
                    .orElse(null);
            case "DOCTOR" -> doctorRepository.findByEmail(username)
                    .map(doctor -> authenticateAccount(doctor.getEmail(), doctor.getPassword(), password, "DOCTOR",
                            newPassword -> {
                                doctor.setPassword(newPassword);
                                doctorRepository.save(doctor);
                            }))
                    .orElse(null);
            case "PATIENT" -> patientRepository.findByEmail(username)
                    .map(patient -> authenticateAccount(patient.getEmail(), patient.getPassword(), password, "PATIENT",
                            newPassword -> {
                                patient.setPassword(newPassword);
                                patientRepository.save(patient);
                            }))
                    .orElse(null);
            default -> null;
        };

        if (user == null) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        UsernamePasswordAuthenticationToken authenticated =
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        authenticated.setDetails(details);
        return authenticated;
    }

    private UserDetails authenticateAccount(String username, String storedPassword, String rawPassword,
                                            String role, Consumer<String> passwordUpdater) {
        if (storedPassword == null) {
            return null;
        }
        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return userDetails(username, storedPassword, role);
        }
        if (storedPassword.equals(rawPassword)) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            passwordUpdater.accept(encodedPassword);
            return userDetails(username, encodedPassword, role);
        }
        return null;
    }

    private UserDetails userDetails(String username, String password, String role) {
        return User.withUsername(username)
                .password(password)
                .roles(role)
                .build();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
