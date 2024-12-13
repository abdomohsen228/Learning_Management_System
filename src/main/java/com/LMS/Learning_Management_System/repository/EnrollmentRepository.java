package com.LMS.Learning_Management_System.repository;

import com.LMS.Learning_Management_System.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
}