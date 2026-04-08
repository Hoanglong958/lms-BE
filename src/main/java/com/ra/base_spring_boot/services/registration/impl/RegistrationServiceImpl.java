package com.ra.base_spring_boot.services.registration.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ra.base_spring_boot.dto.Registration.RegistrationRequestDTO;
import com.ra.base_spring_boot.dto.Registration.RegistrationResponseDTO;
import com.ra.base_spring_boot.dto.Registration.SepayWebhookDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.NotificationType;
import com.ra.base_spring_boot.model.constants.PaymentStatus;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.repository.course.IClassCourseRepository;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.registration.IRegistrationRepository;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements IRegistrationService {

    private static final Pattern TRANSFER_REF_PATTERN = Pattern.compile("(?i)(SEPAY|TUITION)(\\d+)");

    private final IRegistrationRepository registrationRepository;
    private final ICourseRepository courseRepository;
    private final IClassCourseRepository classCourseRepository;
    private final IClassStudentRepository classStudentRepository;
    private final IUserNotificationService userNotificationService;

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
        registration.setTransferRef("SEPAY" + registration.getId());
        registration = registrationRepository.save(registration);

        // Send Notification
        userNotificationService.sendNotification(
            student,
            "Đăng ký khóa học thành công",
            "Bạn đã đăng ký khóa học " + course.getTitle() + ". Vui lòng chuyển khoản đúng mã để hệ thống tự động xác nhận và xếp lớp.",
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
    public RegistrationResponseDTO processSepayWebhook(SepayWebhookDTO payload) {
        String transferContent = payload.getTransferContent();
        String transferRef = extractTransferRef(transferContent);
        
        // 1. Try finding by exact transfer_ref (e.g., "SEPAY13117")
        Registration registration = registrationRepository.findByTransferRefIgnoreCase(transferRef)
                .orElseGet(() -> {
                    // 2. Try swapping prefixes (SEPAY <-> TUITION)
                    String alt = toAltTransferRef(transferRef);
                    return alt != null ? registrationRepository.findByTransferRefIgnoreCase(alt).orElse(null) : null;
                });

        // 3. Fallback: Try finding by numeric ID if prefix-based search fails
        if (registration == null) {
            try {
                Long id = extractNumericId(transferContent);
                if (id != null) {
                    registration = registrationRepository.findById(id).orElse(null);
                }
            } catch (Exception e) {
                // Ignore parsing errors for fallback
            }
        }

        if (registration == null) {
            throw new HttpBadRequest("Không tìm thấy đăng ký khớp mã chuyển khoản " + transferRef + ".");
        }

        if (registration.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new HttpBadRequest("Đăng ký này đã bị hủy, không thể xác nhận thanh toán.");
        }

        BigDecimal transferAmount = payload.getTransferAmount();
        if (transferAmount == null) {
            throw new HttpBadRequest("Webhook SePay không có số tiền giao dịch.");
        }

        if (registration.getAmount() == null || registration.getAmount().compareTo(transferAmount) != 0) {
            throw new HttpBadRequest("Số tiền giao dịch không khớp với học phí cần thanh toán.");
        }

        return completePayment(registration);
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
    public byte[] exportToExcel() {
        List<Registration> registrations = registrationRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Registrations");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Student Name", "Email", "Phone", "Course", "Amount", "Status", "Date", "Transfer Ref" };
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
                row.createCell(8).setCellValue(reg.getTransferRef() != null ? reg.getTransferRef() : "N/A");
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

            addTableCell(table, "Payment Date / Ngày thanh toán:", boldFont);
            addTableCell(table,
                    reg.getPaymentDate() != null
                            ? reg.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            : "N/A",
                    normalFont);

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
                .refundRequested(registration.getRefundRequested())
                .enrolledClassName(enrolledClassName)
                .build();
    }

    private RegistrationResponseDTO completePayment(Registration registration) {
        if (registration.getPaymentStatus() == PaymentStatus.PAID) {
            return toDto(registration);
        }

        registration.setPaymentStatus(PaymentStatus.PAID);
        registration.setPaymentDate(LocalDateTime.now());
        registrationRepository.save(registration);

        userNotificationService.sendNotification(
                registration.getStudent(),
                "Thanh toán thành công",
                "Hệ thống đã xác nhận thanh toán cho khóa học " + registration.getCourse().getTitle() + ".",
                NotificationType.PAYMENT,
                "/registrations?courseId=" + registration.getCourse().getId());

        String enrolledClassName = null;
        List<ClassCourse> classCourses = classCourseRepository.findByCourse_Id(registration.getCourse().getId());
        if (!classCourses.isEmpty()) {
            com.ra.base_spring_boot.model.Class aClass = classCourses.stream()
                    .map(ClassCourse::getClazz)
                    .max((c1, c2) -> c1.getId().compareTo(c2.getId()))
                    .orElse(null);

            if (aClass != null && !classStudentRepository.existsByClassroomIdAndStudentId(
                    aClass.getId(), registration.getStudent().getId())) {
                ClassStudent enrollment = ClassStudent.builder()
                        .classroom(aClass)
                        .student(registration.getStudent())
                        .status(com.ra.base_spring_boot.model.constants.ClassEnrollmentStatus.ACTIVE)
                        .enrolledAt(LocalDateTime.now())
                        .build();
                classStudentRepository.save(enrollment);
                enrolledClassName = aClass.getClassName();

                userNotificationService.sendNotification(
                        registration.getStudent(),
                        "Chào mừng bạn đã vào lớp",
                        "Bạn đã được thêm vào lớp " + enrolledClassName + " cho khóa học "
                                + registration.getCourse().getTitle() + ".",
                        NotificationType.ACADEMIC,
                        "/classes/" + aClass.getId());
            }
        }

        return toDto(registration, enrolledClassName);
    }

    private String extractTransferRef(String transferContent) {
        if (transferContent == null || transferContent.isBlank()) {
            throw new HttpBadRequest("Webhook SePay không có nội dung chuyển khoản.");
        }

        Matcher matcher = TRANSFER_REF_PATTERN.matcher(transferContent);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group().toUpperCase();
        }

        if (lastMatch == null) {
            throw new HttpBadRequest("Không tìm thấy mã thanh toán (SEPAY...) trong nội dung chuyển khoản.");
        }
        return lastMatch;
    }

    private Long extractNumericId(String transferContent) {
        if (transferContent == null) return null;
        // Find the sequence of digits matching our pattern
        Matcher patternMatcher = TRANSFER_REF_PATTERN.matcher(transferContent);
        Long lastId = null;
        while (patternMatcher.find()) {
            lastId = Long.parseLong(patternMatcher.group(2));
        }
        return lastId;
    }

    private String toAltTransferRef(String transferRef) {
        if (transferRef == null) return null;
        if (transferRef.toUpperCase().startsWith("SEPAY")) {
            return "TUITION" + transferRef.substring(5);
        }
        if (transferRef.toUpperCase().startsWith("TUITION")) {
            return "SEPAY" + transferRef.substring(7);
        }
        return null;
    }
}
