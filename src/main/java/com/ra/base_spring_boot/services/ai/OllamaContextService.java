package com.ra.base_spring_boot.services.ai;

import com.ra.base_spring_boot.model.Assignment;
import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.LessonDocument;
import com.ra.base_spring_boot.model.constants.ExamStatus;
import com.ra.base_spring_boot.repository.common.IAssignmentRepository;
import com.ra.base_spring_boot.repository.course.ILessonDocumentRepository;
import com.ra.base_spring_boot.repository.exam.IExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service lấy context dữ liệu từ DB để cung cấp cho Ollama AI.
 */
@Service
@RequiredArgsConstructor
public class OllamaContextService {

    private final IAssignmentRepository assignmentRepository;
    private final IExamRepository examRepository;
    private final ILessonDocumentRepository lessonDocumentRepository;

    private static final DateTimeFormatter VIET_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Lấy context deadline bài tập sắp tới (trong vòng 30 ngày).
     */
    public String buildDeadlineContext() {
        List<Assignment> assignments = assignmentRepository.findAll();

        if (assignments.isEmpty()) {
            return "Hiện tại chưa có bài tập nào trong hệ thống.";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);

        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH BÀI TẬP VÀ DEADLINE:\n");

        int count = 0;
        for (Assignment a : assignments) {
            if (a.getDueDate() != null && a.getDueDate().isBefore(thirtyDaysLater)) {
                String status = a.getDueDate().isBefore(now) ? "ĐÃ HẾT HẠN" : "CÒN HẠN";
                sb.append(String.format(
                        "- Bài tập: \"%s\" | Môn: %s | Deadline: %s | Trạng thái: %s\n",
                        a.getTitle(),
                        a.getCourse() != null ? a.getCourse().getTitle() : "Không rõ",
                        a.getDueDate() != null ? a.getDueDate().format(VIET_DATE_FORMAT) : "Chưa có",
                        status));
                count++;
            }
        }

        if (count == 0) {
            return "Không có bài tập nào có deadline trong 30 ngày tới.";
        }

        return sb.toString();
    }

    /**
     * Lấy context lịch thi sắp tới (status UPCOMING hoặc thi trong vòng 30 ngày).
     */
    public String buildExamContext() {
        List<Exam> exams = examRepository.findAll();

        if (exams.isEmpty()) {
            return "Hiện tại chưa có kỳ thi nào trong hệ thống.";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);

        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH KỲ THI SẮP TỚI:\n");

        int count = 0;
        for (Exam e : exams) {
            boolean isUpcoming = ExamStatus.UPCOMING.equals(e.getStatus());
            boolean isInWindow = e.getStartTime() != null &&
                    e.getStartTime().isAfter(now) &&
                    e.getStartTime().isBefore(thirtyDaysLater);

            if (isUpcoming || isInWindow) {
                sb.append(String.format(
                        "- Kỳ thi: \"%s\" | Bắt đầu: %s | Kết thúc: %s | Thời lượng: %d phút | Điểm tối đa: %d\n",
                        e.getTitle(),
                        e.getStartTime() != null ? e.getStartTime().format(VIET_DATE_FORMAT) : "Chưa xác định",
                        e.getEndTime() != null ? e.getEndTime().format(VIET_DATE_FORMAT) : "Chưa xác định",
                        e.getDurationMinutes() != null ? e.getDurationMinutes() : 0,
                        e.getMaxScore() != null ? e.getMaxScore() : 100));
                count++;
            }
        }

        if (count == 0) {
            sb.append("Không có kỳ thi nào sắp diễn ra trong 30 ngày tới.\n");
        }

        return sb.toString();
    }

    /**
     * Lấy context tài liệu theo lessonId.
     */
    public String buildMaterialContext(Long lessonId) {
        if (lessonId == null) {
            return "Vui lòng cung cấp ID bài học để tra cứu tài liệu.";
        }

        List<LessonDocument> docs = lessonDocumentRepository.findByLesson_IdOrderBySortOrderAsc(lessonId);

        if (docs.isEmpty()) {
            return "Bài học này chưa có tài liệu nào.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("TÀI LIỆU BÀI HỌC (lessonId=%d):\n", lessonId));

        for (LessonDocument doc : docs) {
            sb.append(String.format("- Tài liệu: \"%s\"\n", doc.getTitle()));
            if (doc.getPdfUrl() != null && !doc.getPdfUrl().isEmpty()) {
                sb.append(String.format("  PDF: %s\n", doc.getPdfUrl()));
            }
            if (doc.getVideoUrl() != null && !doc.getVideoUrl().isEmpty()) {
                sb.append(String.format("  Video: %s\n", doc.getVideoUrl()));
            }
            // Tóm tắt nội dung nếu có
            if (doc.getContent() != null && !doc.getContent().isEmpty()) {
                String preview = doc.getContent().length() > 300
                        ? doc.getContent().substring(0, 300) + "..."
                        : doc.getContent();
                sb.append(String.format("  Nội dung tóm tắt: %s\n", preview));
            }
        }

        return sb.toString();
    }

    /**
     * Lấy nội dung bài học để hỏi đáp QA.
     */
    public String buildQAContext(Long lessonId) {
        if (lessonId == null) {
            return "";
        }

        List<LessonDocument> docs = lessonDocumentRepository.findByLesson_IdOrderBySortOrderAsc(lessonId);

        if (docs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("NỘI DUNG BÀI HỌC:\n");
        for (LessonDocument doc : docs) {
            sb.append(String.format("=== %s ===\n", doc.getTitle()));
            if (doc.getContent() != null) {
                // Giới hạn 2000 ký tự mỗi tài liệu để không vượt context window
                String content = doc.getContent().length() > 2000
                        ? doc.getContent().substring(0, 2000) + "..."
                        : doc.getContent();
                sb.append(content).append("\n");
            }
        }

        return sb.toString();
    }
}
