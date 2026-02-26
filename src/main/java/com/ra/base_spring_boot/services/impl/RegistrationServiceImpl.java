package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Registration.RegistrationRequestDTO;
import com.ra.base_spring_boot.dto.Registration.RegistrationResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.PaymentStatus;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements IRegistrationService {

    private final IRegistrationRepository registrationRepository;
    private final ICourseRepository courseRepository;
    private final IClassCourseRepository classCourseRepository;
    private final IClassStudentRepository classStudentRepository;

    @Override
    @Transactional
    public RegistrationResponseDTO register(User student, RegistrationRequestDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học!"));

        if (registrationRepository.findByStudent_IdAndCourse_Id(student.getId(), course.getId()).isPresent()) {
            throw new HttpBadRequest("Bạn đã đăng ký khóa học này rồi!");
        }

        Registration registration = Registration.builder()
                .student(student)
                .course(course)
                .amount(course.getTuitionFee() != null ? course.getTuitionFee() : java.math.BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PENDING)
                .note(dto.getNote())
                .build();

        registration = registrationRepository.save(registration);
        // Generate unique transfer reference after we have the ID
        registration.setTransferRef("TUITION" + registration.getId());
        registration = registrationRepository.save(registration);

        return toDto(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getMyRegistrations(Long studentId) {
        return registrationRepository.findByStudent_Id(studentId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getAllRegistrations() {
        return registrationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RegistrationResponseDTO confirmPayment(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy bản ghi đăng ký!"));

        if (registration.getPaymentStatus() == PaymentStatus.PAID) {
            throw new HttpBadRequest("Đăng ký này đã được thanh toán trước đó.");
        }

        registration.setPaymentStatus(PaymentStatus.PAID);
        registration.setPaymentDate(LocalDateTime.now());
        registrationRepository.save(registration);

        // Tự động thêm vào lớp học (lấy lớp học đầu tiên được gán cho khóa học này)
        List<ClassCourse> classCourses = classCourseRepository.findByCourse_Id(registration.getCourse().getId());
        if (!classCourses.isEmpty()) {
            // Lấy class có ID cao nhất (giả định là lớp mới nhất)
            com.ra.base_spring_boot.model.Class aClass = classCourses.stream()
                    .map(ClassCourse::getClazz)
                    .max((c1, c2) -> c1.getId().compareTo(c2.getId()))
                    .orElse(null);

            if (aClass != null && !classStudentRepository.existsByClassroomIdAndStudentId(aClass.getId(),
                    registration.getStudent().getId())) {
                ClassStudent enrollment = ClassStudent.builder()
                        .classroom(aClass)
                        .student(registration.getStudent())
                        .status(com.ra.base_spring_boot.model.constants.ClassEnrollmentStatus.ACTIVE)
                        .enrolledAt(LocalDateTime.now())
                        .build();
                classStudentRepository.save(enrollment);
            }
        }

        return toDto(registration);
    }

    private RegistrationResponseDTO toDto(Registration registration) {
        if (registration == null)
            return null;
        return RegistrationResponseDTO.builder()
                .id(registration.getId())
                .studentId(registration.getStudent() != null ? registration.getStudent().getId() : null)
                .studentName(registration.getStudent() != null ? registration.getStudent().getFullName() : "N/A")
                .courseId(registration.getCourse() != null ? registration.getCourse().getId() : null)
                .courseTitle(registration.getCourse() != null ? registration.getCourse().getTitle() : "N/A")
                .amount(registration.getAmount())
                .paymentStatus(registration.getPaymentStatus())
                .registrationDate(registration.getRegistrationDate())
                .paymentDate(registration.getPaymentDate())
                .note(registration.getNote())
                .transferRef(registration.getTransferRef())
                .build();
    }
}
