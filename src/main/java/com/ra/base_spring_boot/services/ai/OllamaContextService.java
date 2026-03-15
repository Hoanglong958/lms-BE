package com.ra.base_spring_boot.services.ai;

import com.ra.base_spring_boot.model.Assignment;
import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.LessonDocument;
import com.ra.base_spring_boot.model.constants.ExamStatus;
import com.ra.base_spring_boot.repository.common.IAssignmentRepository;
import com.ra.base_spring_boot.repository.course.ILessonDocumentRepository;
import com.ra.base_spring_boot.repository.exam.IExamRepository;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.repository.classroom.IClassTeacherRepository;
import com.ra.base_spring_boot.repository.classroom.IClassRepository;
import com.ra.base_spring_boot.repository.course.IClassCourseRepository;
import com.ra.base_spring_boot.repository.course.ISessionRepository;
import com.ra.base_spring_boot.model.ClassStudent;
import com.ra.base_spring_boot.model.ClassTeacher;
import com.ra.base_spring_boot.model.ClassCourse;
import com.ra.base_spring_boot.model.Session;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service lấy context dữ liệu từ DB để cung cấp cho Ollama AI.
 */
@Service
@RequiredArgsConstructor
public class OllamaContextService {

    private final IAssignmentRepository assignmentRepository;
    private final IExamRepository examRepository;
    private final ILessonDocumentRepository lessonDocumentRepository;
    private final IClassStudentRepository classStudentRepository;
    private final IClassTeacherRepository classTeacherRepository;
    private final IClassRepository classRepository;
    private final IClassCourseRepository classCourseRepository;
    private final ISessionRepository sessionRepository;

    private static final DateTimeFormatter VIET_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MyUserDetails) {
            return ((MyUserDetails) authentication.getPrincipal()).getUser();
        }
        return null;
    }

    @Transactional(readOnly = true)
    private List<Long> getUserCourseIds(User user) {
        List<Long> courseIds = new ArrayList<>();
        if (user == null) return courseIds;

        if (user.getRole() == RoleName.ROLE_USER) {
            List<ClassStudent> enrolled = classStudentRepository.findByStudent_Id(user.getId());
            for (ClassStudent cs : enrolled) {
                List<ClassCourse> ccs = classCourseRepository.findByClazzId(cs.getClassroom().getId());
                for (ClassCourse cc : ccs) {
                    if (cc.getCourse() != null) {
                        courseIds.add(cc.getCourse().getId());
                    }
                }
            }
        } else if (user.getRole() == RoleName.ROLE_TEACHER) {
            List<ClassTeacher> assigned = classTeacherRepository.findByTeacherId(user.getId());
            for (ClassTeacher ct : assigned) {
                List<ClassCourse> ccs = classCourseRepository.findByClazzId(ct.getClazz().getId());
                for (ClassCourse cc : ccs) {
                    if (cc.getCourse() != null) {
                        courseIds.add(cc.getCourse().getId());
                    }
                }
            }
        }
        return courseIds.stream().distinct().collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private List<Long> getUserClassIds(User user) {
        List<Long> classIds = new ArrayList<>();
        if (user == null) return classIds;

        if (user.getRole() == RoleName.ROLE_USER) {
            List<ClassStudent> enrolled = classStudentRepository.findByStudent_Id(user.getId());
            classIds = enrolled.stream().map(cs -> cs.getClassroom().getId()).collect(Collectors.toList());
        } else if (user.getRole() == RoleName.ROLE_TEACHER) {
            List<ClassTeacher> assigned = classTeacherRepository.findByTeacherId(user.getId());
            classIds = assigned.stream().map(ct -> ct.getClazz().getId()).collect(Collectors.toList());
        }
        return classIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Lấy context deadline bài tập sắp tới (trong vòng 30 ngày).
     */
    @Transactional(readOnly = true)
    public String buildDeadlineContext() {
        User user = getCurrentUser();
        List<Long> userCourseIds = getUserCourseIds(user);
        List<Assignment> assignments = assignmentRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH BÀI TẬP VÀ DEADLINE:\n");

        int count = 0;
        for (Assignment a : assignments) {
            boolean isRelevant = user == null || user.getRole() == RoleName.ROLE_ADMIN || 
                (a.getCourse() != null && userCourseIds.contains(a.getCourse().getId()));

            if (isRelevant && a.getDueDate() != null && 
                a.getDueDate().isAfter(sevenDaysAgo) && 
                a.getDueDate().isBefore(thirtyDaysLater)) {
                
                String status = a.getDueDate().isBefore(now) ? "ĐÃ QUÁ HẠN" : "CÒN HẠN";
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
            String roleStr = (user != null && user.getRole() == RoleName.ROLE_TEACHER) ? "giảng dạy" : "theo học";
            return String.format("Hiện tại hệ thống không thấy bài tập nào sắp đến hạn (trong 30 ngày tới) cho các khóa học bạn %s.", roleStr);
        }

        return sb.toString();
    }

    /**
     * Lấy context lịch thi sắp tới (status UPCOMING hoặc thi trong vòng 30 ngày).
     */
    @Transactional(readOnly = true)
    public String buildExamContext() {
        User user = getCurrentUser();
        List<Long> userCourseIds = getUserCourseIds(user);
        List<Long> userClassIds = getUserClassIds(user);

        List<Exam> exams = examRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);

        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH KỲ THI SẮP TỚI:\n");

        int count = 0;
        for (Exam e : exams) {
            boolean isRelevant = user == null || user.getRole() == RoleName.ROLE_ADMIN || 
                (e.getCourseId() != null && userCourseIds.contains(e.getCourseId())) ||
                (e.getClassId() != null && userClassIds.contains(e.getClassId()));

            if (!isRelevant) continue;

            ExamStatus status = e.getStatus();
            boolean isUpcomingOrOngoing = status == ExamStatus.UPCOMING || status == ExamStatus.ONGOING;
            boolean isInWindow = e.getStartTime() != null &&
                    e.getStartTime().isAfter(now.minusDays(1)) && 
                    e.getStartTime().isBefore(thirtyDaysLater);
            
            if (isUpcomingOrOngoing || isInWindow) {
                sb.append(String.format(
                        "- Kỳ thi: \"%s\" | Bắt đầu: %s | Kết thúc: %s | Trạng thái: %s | Thời lượng: %d phút | Điểm tối đa: %d\n",
                        e.getTitle(),
                        e.getStartTime() != null ? e.getStartTime().format(VIET_DATE_FORMAT) : "Chưa xác định",
                        e.getEndTime() != null ? e.getEndTime().format(VIET_DATE_FORMAT) : "Chưa xác định",
                        status,
                        e.getDurationMinutes() != null ? e.getDurationMinutes() : 0,
                        e.getMaxScore() != null ? e.getMaxScore() : 100));
                count++;
            }
        }

        if (count == 0) {
            String roleStr = (user != null && user.getRole() == RoleName.ROLE_TEACHER) ? "giảng dạy" : "theo học";
            return String.format("Hiện tại hệ thống không thấy kỳ thi nào sắp diễn ra (trong 30 ngày tới) cho các lớp bạn %s.", roleStr);
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

    /**
     * Lấy context lịch học (Session) của sinh viên.
     */
    @Transactional(readOnly = true)
    public String buildScheduleContext() {
        User user = getCurrentUser();
        if (user == null) return "Vui lòng đăng nhập để xem lịch học.";

        List<Long> userClassIds = getUserClassIds(user);
        if (userClassIds.isEmpty()) return "Bạn chưa tham gia lớp học nào.";

        StringBuilder sb = new StringBuilder();
        sb.append("THỜI KHÓA BIỂU CÁC LỚP BẠN ĐANG PHỤ TRÁCH/THEO HỌC:\n");

        for (Long classId : userClassIds) {
            com.ra.base_spring_boot.model.Class clazz = classRepository.findById(classId).orElse(null);
            if (clazz == null) continue;

            sb.append(String.format("\n[Lớp: %s] Bắt đầu: %s - Kết thúc: %s\n", 
                clazz.getClassName(), 
                clazz.getStartDate(), 
                clazz.getEndDate() != null ? clazz.getEndDate() : "Chưa rõ"));
            
            List<ClassCourse> classCourses = classCourseRepository.findByClazzId(clazz.getId());
            for (ClassCourse cc : classCourses) {
                if (cc.getCourse() != null) {
                    sb.append(String.format("  - Khóa: %s\n", cc.getCourse().getTitle()));
                    List<Session> sessions = sessionRepository.findByCourse_IdOrderByOrderIndexAsc(cc.getCourse().getId());
                    if (sessions.isEmpty()) {
                        sb.append("    (Chưa có buổi học nào)\n");
                    } else {
                        for (Session s : sessions) {
                            sb.append(String.format("    + Buổi %d: %s\n", s.getOrderIndex(), s.getTitle()));
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Lấy context giảng viên giảng dạy của sinh viên.
     */
    @Transactional(readOnly = true)
    public String buildTeacherContext() {
        User user = getCurrentUser();
        if (user == null || user.getRole() != RoleName.ROLE_USER) return "Chỉ học viên mới có thông tin giảng viên.";

        List<ClassStudent> enrolled = classStudentRepository.findByStudent_Id(user.getId());
        if (enrolled.isEmpty()) return "Bạn chưa tham gia lớp học nào nên chưa có giảng viên.";

        StringBuilder sb = new StringBuilder();
        sb.append("THÔNG TIN GIẢNG VIÊN ĐANG GIẢNG DẠY BẠN:\n");

        for (ClassStudent cs : enrolled) {
            com.ra.base_spring_boot.model.Class clazz = cs.getClassroom();
            List<ClassTeacher> teachers = classTeacherRepository.findByClazzId(clazz.getId());
            
            sb.append(String.format("\n- Lớp %s:\n", clazz.getClassName()));
            if (teachers.isEmpty()) {
                sb.append("  (Chưa có giảng viên nào được phân công)\n");
            } else {
                for (ClassTeacher ct : teachers) {
                    User teacher = ct.getTeacher();
                    sb.append(String.format("  + %s (Vai trò: %s)\n", teacher.getFullName() != null ? teacher.getFullName() : teacher.getGmail(), ct.getRole() != null ? ct.getRole().name() : "Không rõ"));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Lấy context thông tin các lớp học đang tham gia.
     */
    @Transactional(readOnly = true)
    public String buildClassContext() {
        User user = getCurrentUser();
        if (user == null || user.getRole() != RoleName.ROLE_USER) return "Chỉ học viên mới có thể xem lớp học đang tham gia.";

        List<ClassStudent> enrolled = classStudentRepository.findByStudent_Id(user.getId());
        if (enrolled.isEmpty()) return "Hiện tại bạn chưa đăng ký lớp học nào.";

        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH LỚP HỌC BẠN ĐANG THAM GIA:\n");

        for (ClassStudent cs : enrolled) {
            com.ra.base_spring_boot.model.Class clazz = cs.getClassroom();
            sb.append(String.format("- Lớp: %s | Trạng thái: %s | Ngày bắt đầu: %s\n", 
                clazz.getClassName(), 
                cs.getStatus() != null ? cs.getStatus().name() : "ACTIVE", 
                clazz.getStartDate()));
        }
        return sb.toString();
    }
}
