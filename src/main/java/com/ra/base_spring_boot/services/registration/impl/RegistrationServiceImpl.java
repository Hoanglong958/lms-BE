package com.ra.base_spring_boot.services.registration.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import java.io.File;
import com.ra.base_spring_boot.dto.Registration.RegistrationRequestDTO;
import com.ra.base_spring_boot.dto.Registration.RegistrationResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.PaymentStatus;
import com.ra.base_spring_boot.model.constants.NotificationType;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.repository.course.IClassCourseRepository;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.registration.IRegistrationRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.notification.IUserNotificationService;
import com.ra.base_spring_boot.services.registration.IRegistrationService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements IRegistrationService {

    private final IRegistrationRepository registrationRepository;
    private final ICourseRepository courseRepository;
    private final IClassCourseRepository classCourseRepository;
    private final IClassStudentRepository classStudentRepository;
    private final IUserNotificationService userNotificationService;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public RegistrationResponseDTO register(User student, RegistrationRequestDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học!"));

        if (registrationRepository.findByStudent_IdAndCourse_Id(student.getId(), course.getId()).stream()
                .anyMatch(r -> r.getPaymentStatus() != PaymentStatus.CANCELLED)) {
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

        // Send Notification
        userNotificationService.sendNotification(
            student,
            "Đăng ký khóa học thành công",
            "Bạn đã đăng ký khóa học " + course.getTitle() + ". Vui lòng thực hiện thanh toán để hoàn tất quá trình vào lớp.",
            NotificationType.COURSE_REGISTRATION,
            "/registrations?courseId=" + course.getId() + "&payment=true"
        );

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

        if (registration.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new HttpBadRequest("Chỉ những đăng ký đang chờ thanh toán mới được xác nhận.");
        }

        if (!Boolean.TRUE.equals(registration.getPaymentSubmitted())) {
            throw new HttpBadRequest("Sinh viên chưa báo đã chuyển tiền. Vui lòng chờ xác nhận từ học viên.");
        }

        registration.setPaymentStatus(PaymentStatus.PAID);
        registration.setPaymentDate(LocalDateTime.now());
        registrationRepository.save(registration);

        // Send Payment Success Notification
        userNotificationService.sendNotification(
            registration.getStudent(),
            "Thanh toán thành công",
            "Cảm ơn bạn đã thanh toán cho khóa học " + registration.getCourse().getTitle() + ".",
            NotificationType.PAYMENT,
            "/registrations?courseId=" + registration.getCourse().getId()
        );

        // Tự động thêm vào lớp học (lấy lớp học đầu tiên được gán cho khóa học này)
        String enrolledClassName = null;
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
                enrolledClassName = aClass.getClassName(); // Lưu tên lớp đã thêm

                // Send Class Enrollment Notification
                userNotificationService.sendNotification(
                    registration.getStudent(),
                    "Chào mừng bạn đã vào lớp",
                    "Bạn đã được thêm vào lớp " + enrolledClassName + " cho khóa học " + registration.getCourse().getTitle() + ".",
                    NotificationType.ACADEMIC,
                    "/classes/" + aClass.getId()
                );
            }
        }

        return toDto(registration, enrolledClassName);
    }

    @Override
    @Transactional
    public RegistrationResponseDTO cancelRegistration(Long registrationId, User student) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy bản ghi đăng ký!"));

        if (!registration.getStudent().getId().equals(student.getId())) {
            throw new HttpBadRequest("Bạn không có quyền hủy đăng ký này!");
        }

        if (registration.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new HttpBadRequest("Chỉ có thể hủy đăng ký khi đang ở trạng thái chờ thanh toán!");
        }

        registration.setPaymentStatus(PaymentStatus.CANCELLED);
        registrationRepository.save(registration);

        // Send Cancellation Notification
        userNotificationService.sendNotification(
            student,
            "Hủy đăng ký thành công",
            "Bạn đã hủy đăng ký khóa học " + registration.getCourse().getTitle() + ".",
            NotificationType.COURSE_REGISTRATION,
            "/registrations?courseId=" + registration.getCourse().getId()
        );

        return toDto(registration);
    }

    @Override
    @Transactional
    public RegistrationResponseDTO requestRefund(Long registrationId, User student) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy bản ghi đăng ký!"));

        if (!registration.getStudent().getId().equals(student.getId())) {
            throw new HttpBadRequest("Bạn không có quyền yêu cầu hoàn tiền cho đăng ký này!");
        }

        if (registration.getPaymentStatus() != PaymentStatus.PAID) {
            throw new HttpBadRequest("Chỉ có thể yêu cầu hoàn tiền sau khi đã thanh toán.");
        }

        LocalDateTime eligibleAt = getRefundEligibleAt(registration);
        if (eligibleAt == null || LocalDateTime.now().isBefore(eligibleAt)) {
            throw new HttpBadRequest("Bạn chỉ có thể yêu cầu hoàn tiền sau 3 ngày kể từ ngày thanh toán.");
        }

        if (Boolean.TRUE.equals(registration.getRefundRequested())) {
            throw new HttpBadRequest("Bạn đã gửi yêu cầu hoàn tiền trước đó.");
        }

        registration.setRefundRequested(true);
        registration.setRefundRequestedAt(LocalDateTime.now());
        registration.setRefundConfirmed(false);
        registration.setRefundConfirmedAt(null);
        registration.setPaymentStatus(PaymentStatus.REFUND_REQUESTED);
        registrationRepository.save(registration);

        String adminMessage = "Sinh viên " + registration.getStudent().getFullName()
                + " vừa yêu cầu hoàn tiền cho khóa học " + registration.getCourse().getTitle() + ".";
        List<User> admins = userRepository.findByRole(RoleName.ROLE_ADMIN);
        for (User admin : admins) {
            userNotificationService.sendNotification(
                    admin,
                    "Yêu cầu hoàn tiền",
                    adminMessage,
                    NotificationType.PAYMENT,
                    "/admin/registrations"
            );
        }

        userNotificationService.sendNotification(
                student,
                "Yêu cầu hoàn tiền đã gửi",
                "Yêu cầu hoàn tiền của bạn cho khóa học \"" + registration.getCourse().getTitle()
                        + "\" đã đến tay admin. Họ sẽ phản hồi sớm.",
                NotificationType.PAYMENT,
                "/registrations"
        );

        return toDto(registration);
    }

    @Override
    @Transactional
    public RegistrationResponseDTO markPaymentSubmitted(Long registrationId, User student) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy bản ghi đăng ký!"));

        if (!registration.getStudent().getId().equals(student.getId())) {
            throw new HttpBadRequest("Bạn không có quyền cập nhật thanh toán cho đăng ký này!");
        }

        if (registration.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new HttpBadRequest("Chỉ đăng ký đang ở trạng thái chờ thanh toán mới có thể thông báo.");
        }

        if (Boolean.TRUE.equals(registration.getPaymentSubmitted())) {
            throw new HttpBadRequest("Bạn đã gửi thông báo đã chuyển khoản rồi.");
        }

        registration.setPaymentSubmitted(true);
        registrationRepository.save(registration);
        return toDto(registration);
    }

    @Override
    @Transactional
    public List<RegistrationResponseDTO> confirmBulkPayment(List<Long> registrationIds) {
        return registrationIds.stream()
                .map(this::confirmPayment)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RegistrationResponseDTO confirmRefund(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy bản ghi đăng ký!"));

        if (registration.getPaymentStatus() != PaymentStatus.REFUND_REQUESTED) {
            throw new HttpBadRequest("Chỉ có thể xác nhận hoàn tiền cho đăng ký đang chờ xử lý.");
        }

        // Remove student from any class that the course was mapped to
        if (registration.getCourse() != null && registration.getCourse().getId() != null) {
            List<ClassCourse> assignedClasses = classCourseRepository.findByCourse_Id(registration.getCourse().getId());
            assignedClasses.stream()
                    .map(ClassCourse::getClazz)
                    .filter(clazz -> clazz != null && clazz.getId() != null)
                    .map(clazz -> clazz.getId())
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(classroomId -> classStudentRepository
                            .findByClassroomIdAndStudentId(classroomId, registration.getStudent().getId())
                            .ifPresent(classStudentRepository::delete));
        }

        registration.setRefundConfirmed(true);
        registration.setRefundConfirmedAt(LocalDateTime.now());
        registration.setPaymentStatus(PaymentStatus.REFUNDED);
        registration.setRefundRequested(false);
        registration.setRefundRequestedAt(null);
        registration.setPaymentSubmitted(false);
        registrationRepository.save(registration);

        userNotificationService.sendNotification(
                registration.getStudent(),
                "Hoàn tiền đã được xác nhận",
                "Admin đã xác nhận hoàn tiền cho khóa học " + registration.getCourse().getTitle()
                        + ". Bạn sẽ nhận lại tiền trong thời gian sớm nhất.",
                NotificationType.PAYMENT,
                "/registrations"
        );

        return toDto(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponseDTO getByTransferRef(String transferRef) {
        Registration registration = registrationRepository.findByTransferRef(transferRef)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy giao dịch với mã chuyển khoản này."));
        return toDto(registration);
    }

    @Override
    public byte[] exportToExcel() {
        List<Registration> registrations = registrationRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Registrations");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Student Name", "Email", "Phone", "Course", "Amount", "Status", "Date", "Sinh viên báo" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Registration reg : registrations) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(reg.getId());
                row.createCell(1).setCellValue(reg.getStudent() != null ? reg.getStudent().getFullName() : "N/A");
                row.createCell(2).setCellValue(reg.getStudent() != null ? reg.getStudent().getGmail() : "N/A");
                row.createCell(3).setCellValue(reg.getStudent() != null ? reg.getStudent().getPhone() : "N/A");
                row.createCell(4).setCellValue(reg.getCourse() != null ? reg.getCourse().getTitle() : "N/A");
                row.createCell(5).setCellValue(reg.getAmount() != null ? reg.getAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(reg.getPaymentStatus() != null ? reg.getPaymentStatus().name() : "N/A");
                row.createCell(7).setCellValue(
                        reg.getRegistrationDate() != null ? reg.getRegistrationDate().format(formatter) : "N/A");
                row.createCell(8).setCellValue(reg.getPaymentSubmitted() != null && reg.getPaymentSubmitted() ? "Có" : "Chưa");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long registrationId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new HttpBadRequest("Registration not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Load NotoSans font for Vietnamese support
            BaseFont notoSansBase = loadNotoSansFont();

            // Font setup with Vietnamese support
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(notoSansBase, 18, com.lowagie.text.Font.BOLD, Color.BLACK);
            com.lowagie.text.Font normalFont = new com.lowagie.text.Font(notoSansBase, 12, com.lowagie.text.Font.NORMAL, Color.BLACK);
            com.lowagie.text.Font boldFont = new com.lowagie.text.Font(notoSansBase, 12, com.lowagie.text.Font.BOLD, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("PAYMENT INVOICE / HOÁ ĐƠN THANH TOÁN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Invoice details
            document.add(new Paragraph("Invoice ID: #" + reg.getId(), boldFont));
            document.add(new Paragraph(
                    "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
            document.add(new Paragraph("Status: " + reg.getPaymentStatus(), boldFont));
            document.add(new Chunk("\n"));

            // Table for info
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            addTableCell(table, "Student / Học viên:", boldFont);
            addTableCell(table, reg.getStudent() != null ? reg.getStudent().getFullName() : "N/A", normalFont);

            addTableCell(table, "Email:", boldFont);
            addTableCell(table, reg.getStudent() != null ? reg.getStudent().getGmail() : "N/A", normalFont);

            addTableCell(table, "Course / Khóa học:", boldFont);
            addTableCell(table, reg.getCourse() != null ? reg.getCourse().getTitle() : "N/A", normalFont);

            addTableCell(table, "Amount / Số tiền:", boldFont);
            addTableCell(table, reg.getAmount() != null ? String.format("%,.0f VND", reg.getAmount()) : "0 VND",
                    boldFont);

            addTableCell(table, "Ref / Mã tham chiếu:", boldFont);
            addTableCell(table, reg.getTransferRef() != null ? reg.getTransferRef() : "N/A", normalFont);

            addTableCell(table, "Sinh viên báo:", boldFont);
            addTableCell(table, Boolean.TRUE.equals(reg.getPaymentSubmitted()) ? "Đã gửi" : "Chưa gửi", normalFont);

            document.add(table);

            document.add(new Chunk("\n\n"));
            Paragraph footer = new Paragraph("Thank you for your payment! / Cảm ơn bạn đã thanh toán!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addTableCell(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }

    private BaseFont loadNotoSansFont() {
        try {
            // Try file system paths first (works in dev mode)
            String[] filePaths = {
                System.getProperty("user.dir") + "/be/src/main/resources/static/NotoSans-Regular.ttf",
                System.getProperty("user.dir") + "/src/main/resources/static/NotoSans-Regular.ttf",
                "src/main/resources/static/NotoSans-Regular.ttf",
                "be/src/main/resources/static/NotoSans-Regular.ttf"
            };
            
            for (String path : filePaths) {
                java.io.File fontFile = new java.io.File(path);
                if (fontFile.exists()) {
                    System.out.println("Loading NotoSans font from: " + path);
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            }
            
            // Try classpath resources (works in production jar)
            try (var fontStream = getClass().getClassLoader().getResourceAsStream("static/NotoSans-Regular.ttf")) {
                if (fontStream != null) {
                    // Copy to temp file but DO NOT delete it - OpenPDF needs it
                    byte[] fontBytes = fontStream.readAllBytes();
                    java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("fonts");
                    java.nio.file.Path tempPath = tempDir.resolve("NotoSans-Regular.ttf");
                    java.nio.file.Files.write(tempPath, fontBytes);
                    System.out.println("Loading NotoSans font from classpath to temp: " + tempPath);
                    return BaseFont.createFont(tempPath.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            }
            
            System.err.println("Warning: Could not load NotoSans font, using Helvetica fallback");
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            
        } catch (Exception e) {
            System.err.println("Error loading font: " + e.getMessage());
            e.printStackTrace();
            try {
                return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            } catch (Exception fallbackException) {
                throw new RuntimeException("Failed to load any font: " + fallbackException.getMessage());
            }
        }
    }

    private RegistrationResponseDTO toDto(Registration registration) {
        return toDto(registration, null);
    }

    private RegistrationResponseDTO toDto(Registration registration, String enrolledClassName) {
        if (registration == null)
            return null;
        LocalDateTime eligibleAt = getRefundEligibleAt(registration);
        boolean canRequestRefund = isRefundRequestAllowed(registration, eligibleAt);
        return RegistrationResponseDTO.builder()
                .id(registration.getId())
                .studentId(registration.getStudent() != null ? registration.getStudent().getId() : null)
                .studentName(registration.getStudent() != null ? registration.getStudent().getFullName() : "N/A")
                .studentEmail(registration.getStudent() != null ? registration.getStudent().getGmail() : "N/A")
                .studentPhone(registration.getStudent() != null ? registration.getStudent().getPhone() : "N/A")
                .courseId(registration.getCourse() != null ? registration.getCourse().getId() : null)
                .courseTitle(registration.getCourse() != null ? registration.getCourse().getTitle() : "N/A")
                .amount(registration.getAmount())
                .paymentStatus(registration.getPaymentStatus())
                .registrationDate(registration.getRegistrationDate())
                .paymentDate(registration.getPaymentDate())
                .note(registration.getNote())
                .transferRef(registration.getTransferRef())
                .paymentSubmitted(registration.getPaymentSubmitted())
                .refundRequested(registration.getRefundRequested())
                .refundRequestedAt(registration.getRefundRequestedAt())
                .refundConfirmed(registration.getRefundConfirmed())
                .refundConfirmedAt(registration.getRefundConfirmedAt())
                .refundEligibleAt(eligibleAt)
                .canRequestRefund(canRequestRefund)
                .enrolledClassName(enrolledClassName)
                .build();
    }

    private LocalDateTime getRefundEligibleAt(Registration registration) {
        if (registration == null || registration.getPaymentDate() == null) {
            return null;
        }
        return registration.getPaymentDate().plusDays(3);
    }

    private boolean isRefundRequestAllowed(Registration registration, LocalDateTime eligibleAt) {
        if (registration == null || eligibleAt == null) {
            return false;
        }
        if (registration.getPaymentStatus() != PaymentStatus.PAID) {
            return false;
        }
        if (Boolean.TRUE.equals(registration.getRefundRequested())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(eligibleAt);
    }
}
