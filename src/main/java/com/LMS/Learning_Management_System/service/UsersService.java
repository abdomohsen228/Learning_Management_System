package com.LMS.Learning_Management_System.service;

import com.LMS.Learning_Management_System.entity.*;
import com.LMS.Learning_Management_System.repository.*;
import com.LMS.Learning_Management_System.util.UserSignUpRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersTypeRepository usersTypeRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final InstructorRepository instructorRepository;
    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, UsersTypeRepository usersTypeRepository, StudentRepository studentRepository, AdminRepository adminRepository, InstructorRepository instructorRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
        this.usersTypeRepository = usersTypeRepository;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.instructorRepository = instructorRepository;
    }
    public void save(UserSignUpRequest signUpRequest , HttpServletRequest request) {
        Users loggedInUser = (Users) request.getSession().getAttribute("user");
        if (loggedInUser == null) {
            throw new IllegalArgumentException("Admin must logged in to create a new user");
        }
        if (loggedInUser.getUserTypeId().getUserTypeId() !=1) {
            throw new IllegalArgumentException("Admin only can create account");
        }
        if (usersRepository.findByEmail(signUpRequest.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }
        UsersType userType = usersTypeRepository.findById(signUpRequest.getUserTypeId())
                .orElseThrow(() -> new EntityNotFoundException("User Type not found"));
        Users newUser = new Users(
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                userType
        );
        newUser.setRegistrationDate(new Date());
        usersRepository.save(newUser);

        if(newUser.getUserTypeId().getUserTypeId()==1)
        {
            adminRepository.save(new Admin(newUser));
        }
        else if(newUser.getUserTypeId().getUserTypeId()==2)
            studentRepository.save(new Student(newUser));
        else{
            instructorRepository.save(new Instructor(newUser));
        }
    }
    public Users findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}