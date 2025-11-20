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
    // Dùng email để đăng nhập
    Optional<User> findByEmail(String email);

    // Dùng cho quên mật khẩu
    Optional<User> findByResetToken(String token);

    // Kiểm tra email tồn tại (đăng ký)
    boolean existsByEmail(String email);

    // Kiểm tra email tồn tại (không phân biệt hoa thường)
    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:role IS NULL OR u.role = :role) AND (:active IS NULL OR u.isActive = :active)")
    Page<User> search(@Param("keyword") String keyword,
                      @Param("role") RoleName role,
                      @Param("active") Boolean active,
                      Pageable pageable);
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(RoleName role);

    // count users created since
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :since")
    long countByRoleSince(RoleName role, LocalDateTime since);

    // count users created before given date (useful for prev period)
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt < :before")
    long countByRoleBefore(RoleName role, LocalDateTime before);

    // find new users since date
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findNewUsersSince(RoleName role, LocalDateTime since);

    // top students (if averageScore stored in user or join with UserCourse/Exam)
    @Query("SELECT u FROM User u JOIN UserCourse uc ON uc.user = u GROUP BY u.id ORDER BY AVG(uc.averageScore) DESC")
    List<User> findTopStudents(Pageable pageable);

    // fallback: if no averageScore, take by recent created
    Page<User> findAll(Pageable pageable);


}
