package com.ra.base_spring_boot.services.common.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.PaymentStatus;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.course.ILessonQuizRepository;
import com.ra.base_spring_boot.repository.exam.IExamAttemptRepository;
import com.ra.base_spring_boot.repository.quiz.IQuizResultRepository;
import com.ra.base_spring_boot.repository.registration.IRegistrationRepository;
import com.ra.base_spring_boot.repository.user.IUserCourseRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.common.IReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final IUserRepository userRepository;
    private final ICourseRepository courseRepository;
    private final IRegistrationRepository registrationRepository;
    private final IUserCourseRepository userCourseRepository;
    private final IQuizResultRepository quizResultRepository;
    private final ILessonQuizRepository lessonQuizRepository;
    private final IExamAttemptRepository examAttemptRepository;
    private final IClassStudentRepository classStudentRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] exportUsers(String type) {
        List<User> users = userRepository.findAll();
        if ("excel".equalsIgnoreCase(type)) {
            return generateExcel("Báo cáo người dùng",
                    new String[] { "ID", "Họ tên", "Email", "Vai trò", "Trạng thái", "Ngày tạo" }, users,
                    (row, user, style) -> {
                        row.createCell(0).setCellValue(user.getId());
                        row.createCell(1).setCellValue(user.getFullName());
                        row.createCell(2).setCellValue(user.getGmail());
                        row.createCell(3).setCellValue(user.getRole().name());
                        row.createCell(4).setCellValue(
                                user.getIsActive() != null && user.getIsActive() ? "Hoạt động" : "Bị khóa");
                        row.createCell(5).setCellValue(user.getCreatedAt().format(formatter));
                    });
        } else {
            return generatePdf("BÁO CÁO NGƯỜI DÙNG",
                    new String[] { "ID", "Họ tên", "Email", "Vai trò", "Trạng thái", "Ngày tạo" }, users,
                    (table, user, font) -> {
                        addTableCell(table, String.valueOf(user.getId()), font);
                        addTableCell(table, user.getFullName(), font);
                        addTableCell(table, user.getGmail(), font);
                        addTableCell(table, user.getRole().name(), font);
                        addTableCell(table, user.getIsActive() != null && user.getIsActive() ? "Hoạt động" : "Bị khóa",
                                font);
                        addTableCell(table, user.getCreatedAt().format(formatter), font);
                    });
        }
    }

    @Override
    public byte[] exportCourses(String type) {
        List<Course> courses = courseRepository.findAll();
        if ("excel".equalsIgnoreCase(type)) {
            return generateExcel("Báo cáo khóa học",
                    new String[] { "ID", "Tiêu đề", "Cấp độ", "Giá", "Học viên", "Tạo ngày" }, courses,
                    (row, course, style) -> {
                        row.createCell(0).setCellValue(course.getId());
                        row.createCell(1).setCellValue(course.getTitle());
                        row.createCell(2).setCellValue(course.getLevel().name());
                        row.createCell(3).setCellValue(
                                course.getTuitionFee() != null ? course.getTuitionFee().doubleValue() : 0);
                        long studentCount = userCourseRepository.countByCourse_Id(course.getId());
                        row.createCell(4).setCellValue(studentCount);
                        row.createCell(5).setCellValue(course.getCreatedAt().format(formatter));
                    });
        } else {
            return generatePdf("BÁO CÁO KHÓA HỌC",
                    new String[] { "ID", "Tiêu đề", "Cấp độ", "Giá", "Học viên", "Ngày tạo" }, courses,
                    (table, course, font) -> {
                        addTableCell(table, String.valueOf(course.getId()), font);
                        addTableCell(table, course.getTitle(), font);
                        addTableCell(table, course.getLevel().name(), font);
                        addTableCell(table,
                                String.format("%,.0f",
                                        course.getTuitionFee() != null ? course.getTuitionFee().doubleValue() : 0),
                                font);
                        long studentCount = userCourseRepository.countByCourse_Id(course.getId());
                        addTableCell(table, String.valueOf(studentCount), font);
                        addTableCell(table, course.getCreatedAt().format(formatter), font);
                    });
        }
    }

    @Override
    public byte[] exportStudentProgress(String type) {
        List<UserCourse> progresses = userCourseRepository.findAll();
        if ("excel".equalsIgnoreCase(type)) {
            return generateExcel("Báo cáo tiến độ", new String[] { "Học viên", "Khóa học", "Tiến độ (%)", "Hoàn thành",
                    "Điểm trung bình", "Ngày đăng ký" }, progresses, (row, uc, style) -> {
                        row.createCell(0).setCellValue(uc.getUser() != null ? uc.getUser().getFullName() : "N/A");
                        row.createCell(1).setCellValue(uc.getCourse() != null ? uc.getCourse().getTitle() : "N/A");
                        row.createCell(2).setCellValue(
                                uc.getProgressPercentage() != null ? uc.getProgressPercentage().doubleValue() : 0);
                        row.createCell(3).setCellValue(uc.getCompleted() != null && uc.getCompleted() ? "X" : "");
                        row.createCell(4)
                                .setCellValue(uc.getAverageScore() != null ? uc.getAverageScore().doubleValue() : 0);
                        row.createCell(5).setCellValue(
                                uc.getEnrolledAt() != null ? uc.getEnrolledAt().format(formatter) : "N/A");
                    });
        } else {
            return generatePdf("BÁO CÁO TIẾN ĐỘ HỌC VIÊN",
                    new String[] { "Học viên", "Khóa học", "Tiến độ", "Done", "Điểm TB", "Ngày đăng ký" }, progresses,
                    (table, uc, font) -> {
                        addTableCell(table, uc.getUser() != null ? uc.getUser().getFullName() : "N/A", font);
                        addTableCell(table, uc.getCourse() != null ? uc.getCourse().getTitle() : "N/A", font);
                        addTableCell(table,
                                (uc.getProgressPercentage() != null ? uc.getProgressPercentage() : "0") + "%", font);
                        addTableCell(table, uc.getCompleted() != null && uc.getCompleted() ? "X" : "", font);
                        addTableCell(table, String.valueOf(uc.getAverageScore() != null ? uc.getAverageScore() : "0"),
                                font);
                        addTableCell(table, uc.getEnrolledAt() != null ? uc.getEnrolledAt().format(formatter) : "N/A",
                                font);
                    });
        }
    }

    @Override
    public byte[] exportQuizReports(String type) {
        List<LessonQuiz> quizzes = lessonQuizRepository.findAll();
        if ("excel".equalsIgnoreCase(type)) {
            return generateExcel("Báo cáo bài thi", new String[] { "Tên bài thi", "Bài học", "Số câu hỏi", "Max điểm",
                    "Điểm đạt", "Số lượt thi", "Tỉ lệ đạt (%)" }, quizzes, (row, quiz, style) -> {
                        row.createCell(0).setCellValue(quiz.getTitle());
                        row.createCell(1).setCellValue(quiz.getLesson() != null ? quiz.getLesson().getTitle() : "N/A");
                        row.createCell(2).setCellValue(quiz.getQuestionCount());
                        row.createCell(3).setCellValue(quiz.getMaxScore());
                        row.createCell(4).setCellValue(quiz.getPassingScore());
                        long attempts = Optional.ofNullable(quizResultRepository.countAttemptByQuiz(quiz.getId()))
                                .orElse(0L);
                        row.createCell(5).setCellValue(attempts);
                        long pass = Optional.ofNullable(quizResultRepository.countPassByQuiz(quiz.getId())).orElse(0L);
                        double passRate = attempts == 0 ? 0 : (double) pass / attempts * 100;
                        row.createCell(6).setCellValue(passRate);
                    });
        } else {
            return generatePdf("BÁO CÁO BÀI THI / QUIZ",
                    new String[] { "Tên bài thi", "Bài học", "Câu hỏi", "Max", "Lượt thi", "Tỉ lệ đạt" }, quizzes,
                    (table, quiz, font) -> {
                        addTableCell(table, quiz.getTitle(), font);
                        addTableCell(table, quiz.getLesson() != null ? quiz.getLesson().getTitle() : "N/A", font);
                        addTableCell(table, String.valueOf(quiz.getQuestionCount()), font);
                        addTableCell(table, String.valueOf(quiz.getMaxScore()), font);
                        long attempts = Optional.ofNullable(quizResultRepository.countAttemptByQuiz(quiz.getId()))
                                .orElse(0L);
                        addTableCell(table, String.valueOf(attempts), font);
                        long pass = Optional.ofNullable(quizResultRepository.countPassByQuiz(quiz.getId())).orElse(0L);
                        double passRate = attempts == 0 ? 0 : (double) pass / attempts * 100;
                        addTableCell(table, String.format("%.1f%%", passRate), font);
                    });
        }
    }

    @Override
    public byte[] exportRevenue(String type) {
        List<Registration> registrations = registrationRepository.findAll();
        if ("excel".equalsIgnoreCase(type)) {
            return generateExcel("Báo cáo doanh thu", new String[] { "Mã tham chiếu", "Học viên", "Khóa học", "Số tiền",
                    "Trạng thái", "Ngày thanh toán" }, registrations, (row, reg, style) -> {
                        row.createCell(0).setCellValue(reg.getTransferRef());
                        row.createCell(1)
                                .setCellValue(reg.getStudent() != null ? reg.getStudent().getFullName() : "N/A");
                        row.createCell(2).setCellValue(reg.getCourse() != null ? reg.getCourse().getTitle() : "N/A");
                        row.createCell(3).setCellValue(reg.getAmount() != null ? reg.getAmount().doubleValue() : 0);
                        row.createCell(4)
                                .setCellValue(reg.getPaymentStatus() != null ? reg.getPaymentStatus().name() : "N/A");
                        row.createCell(5)
                                .setCellValue(reg.getPaymentDate() != null ? reg.getPaymentDate().format(formatter)
                                        : "Chưa thanh toán");
                    });
        } else {
            return generatePdf("BÁO CÁO DOANH THU",
                    new String[] { "Ref", "Học viên", "Khóa học", "Số tiền", "Status", "Ngày nộp" }, registrations,
                    (table, reg, font) -> {
                        addTableCell(table, reg.getTransferRef(), font);
                        addTableCell(table, reg.getStudent() != null ? reg.getStudent().getFullName() : "N/A", font);
                        addTableCell(table, reg.getCourse() != null ? reg.getCourse().getTitle() : "N/A", font);
                        addTableCell(table,
                                String.format("%,.0f", reg.getAmount() != null ? reg.getAmount().doubleValue() : 0),
                                font);
                        addTableCell(table, reg.getPaymentStatus() != null ? reg.getPaymentStatus().name() : "N/A",
                                font);
                        addTableCell(table,
                                reg.getPaymentDate() != null ? reg.getPaymentDate().format(formatter) : "N/A", font);
                    });
        }
    }

    // ================= HELPER METHODS FOR EXCEL =================

    @FunctionalInterface
    interface ExcelRowFiller<T> {
        void fill(Row row, T item, CellStyle style);
    }

    private <T> byte[] generateExcel(String sheetName, String[] headers, List<T> items, ExcelRowFiller<T> filler) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (T item : items) {
                Row row = sheet.createRow(rowIdx++);
                filler.fill(row, item, null);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    // ================= HELPER METHODS FOR PDF =================

    @FunctionalInterface
    interface PdfRowFiller<T> {
        void fill(PdfPTable table, T item, Font font);
    }

    private <T> byte[] generatePdf(String titleText, String[] headers, List<T> items, PdfRowFiller<T> filler) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont notoSansBase = loadNotoSansFont();
            Font titleFont = new Font(notoSansBase, 18, Font.BOLD, Color.BLACK);
            Font headerFont = new Font(notoSansBase, 12, Font.BOLD, Color.BLACK);
            Font normalFont = new Font(notoSansBase, 10, Font.NORMAL, Color.BLACK);

            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (T item : items) {
                filler.fill(table, item, normalFont);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "N/A", font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private BaseFont loadNotoSansFont() {
        try {
            String[] filePaths = {
                    System.getProperty("user.dir") + "/be/src/main/resources/static/NotoSans-Regular.ttf",
                    System.getProperty("user.dir") + "/src/main/resources/static/NotoSans-Regular.ttf",
                    "src/main/resources/static/NotoSans-Regular.ttf",
                    "be/src/main/resources/static/NotoSans-Regular.ttf"
            };
            for (String path : filePaths) {
                java.io.File fontFile = new java.io.File(path);
                if (fontFile.exists()) {
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            }
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        } catch (Exception e) {
            try {
                return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load any font", ex);
            }
        }
    }
}
