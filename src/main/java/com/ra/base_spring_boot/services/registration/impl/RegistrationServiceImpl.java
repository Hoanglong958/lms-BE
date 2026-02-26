package com.ra.base_spring_boot.services.registration.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ra.base_spring_boot.dto.Registration.RegistrationRequestDTO;
import com.ra.base_spring_boot.dto.Registration.RegistrationResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.PaymentStatus;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.repository.course.IClassCourseRepository;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.registration.IRegistrationRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    public byte[] exportToExcel() {
        List<Registration> registrations = registrationRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Registrations");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Student Name", "Email", "Phone", "Course", "Amount", "Status", "Date" };
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
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    @Override
    public byte[] generateInvoicePdf(Long registrationId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new HttpBadRequest("Registration not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Font setup (Basic fonts in OpenPDF)
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            com.lowagie.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            com.lowagie.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("PAYMENT INVOICE / HOÁ ĐƠN THANH TOÁN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Invoice details
            document.add(new Paragraph("Invoice ID: #" + reg.getId(), boldFont));
            document.add(new Paragraph(
                    "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
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

            document.add(table);

            document.add(new Chunk("\n\n"));
            Paragraph footer = new Paragraph("Thank you for your payment! / Cảm ơn bạn đã thanh toán!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addTableCell(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }

    private RegistrationResponseDTO toDto(Registration registration) {
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
                .build();
    }
}
