package com.nexora.core.application.management.usecases.commands;

import com.nexora.core.infrastructure.persistence.user.entities.AcademicInterestJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.FacultyJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.AcademicInterestRepository;
import com.nexora.core.infrastructure.persistence.user.repositories.CourseRepository;
import com.nexora.core.infrastructure.persistence.user.repositories.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogManagementUseCase {

    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final AcademicInterestRepository academicInterestRepository;

    // Faculty
    public List<FacultyJpaEntity> getAllFaculties() {
        return facultyRepository.findAllByOrderByNameAsc();
    }

    public FacultyJpaEntity createFaculty(String name) {
        if (facultyRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new IllegalArgumentException("Faculty already exists: " + name);
        }
        return facultyRepository.save(FacultyJpaEntity.builder().name(name.trim()).build());
    }

    public FacultyJpaEntity updateFaculty(UUID id, String name) {
        FacultyJpaEntity faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        faculty.setName(name.trim());
        return facultyRepository.save(faculty);
    }

    public boolean deleteFaculty(UUID id) {
        facultyRepository.deleteById(id);
        return true;
    }

    // Course
    public List<CourseJpaEntity> getAllCourses() {
        return courseRepository.findAllByOrderByNameAsc();
    }

    public CourseJpaEntity createCourse(String name, UUID facultyId) {
        FacultyJpaEntity faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        return courseRepository.save(CourseJpaEntity.builder().name(name.trim()).facultad(faculty).build());
    }

    public CourseJpaEntity updateCourse(UUID id, String name, UUID facultyId) {
        CourseJpaEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        FacultyJpaEntity faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        course.setName(name.trim());
        course.setFacultad(faculty);
        return courseRepository.save(course);
    }

    public boolean deleteCourse(UUID id) {
        courseRepository.deleteById(id);
        return true;
    }

    // Academic Interest
    public List<AcademicInterestJpaEntity> getAllInterests() {
        return academicInterestRepository.findAllByOrderByNameAsc();
    }

    public AcademicInterestJpaEntity createInterest(String name) {
        if (academicInterestRepository.findByName(name.trim()).isPresent()) {
            throw new IllegalArgumentException("Interest already exists: " + name);
        }
        return academicInterestRepository.save(AcademicInterestJpaEntity.builder().name(name.trim()).build());
    }

    public AcademicInterestJpaEntity updateInterest(UUID id, String name) {
        AcademicInterestJpaEntity interest = academicInterestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academic interest not found"));
        interest.setName(name.trim());
        return academicInterestRepository.save(interest);
    }

    public boolean deleteInterest(UUID id) {
        academicInterestRepository.deleteById(id);
        return true;
    }
}
