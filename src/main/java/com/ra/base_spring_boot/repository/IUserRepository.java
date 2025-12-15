package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {

    // Dùng gmail để đăng nhập
    Optional<User> findByGmail(String gmail);

    Optional<User> findByGmailIgnoreCase(String gmail);

    // Dùng cho quên mật khẩu
    Optional<User> findByResetToken(String token);

    // Kiểm tra gmail tồn tại (đăng ký)
    boolean existsByGmail(String gmail);

    // Kiểm tra gmail tồn tại (không phân biệt hoa thường)
    boolean existsByGmailIgnoreCase(String gmail);

    @Query("SELECT u FROM User u WHERE (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:role IS NULL OR u.role = :role) AND (:active IS NULL OR u.isActive = :active) ORDER BY u.createdAt DESC")
    Page<User> search(@Param("keyword") String keyword,
                      @Param("role") RoleName role,
                      @Param("active") Boolean active,
                      Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(RoleName role);

    // Count users created since
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :since")
    long countByRoleSince(RoleName role, LocalDateTime since);

    // Count users created before given date (useful for previous period)
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt < :before")
    long countByRoleBefore(RoleName role, LocalDateTime before);

    // Find new users since date
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findNewUsersSince(RoleName role, LocalDateTime since);

    // Top students (if averageScore stored in user or join with UserCourse/Exam)
    @Query("SELECT u FROM User u JOIN UserCourse uc ON uc.user = u GROUP BY u.id ORDER BY AVG(uc.averageScore) DESC")
    List<User> findTopStudents(Pageable pageable);

    // Fallback: if no averageScore, take by recent created
    Page<User> findAll(Pageable pageable);

    // ======= MỚI THÊM =======

    // Count users by role between 2 thời điểm (dùng cho tăng trưởng theo tuần/tháng)
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :start AND u.createdAt <= :end")
    long countByRoleBetween(@Param("role") RoleName role,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);

    // Lấy danh sách users theo role giữa 2 thời điểm (nếu cần)
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt >= :start AND u.createdAt <= :end ORDER BY u.createdAt DESC")
    List<User> findByRoleBetween(@Param("role") RoleName role,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);
}
