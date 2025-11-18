package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    // ✅ Dùng email để đăng nhập
    Optional<User> findByEmail(String email);

    // ✅ Dùng cho quên mật khẩu
    Optional<User> findByResetToken(String token);

    // ✅ Kiểm tra email tồn tại (đăng ký)
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:role IS NULL OR u.role = :role) AND (:active IS NULL OR u.isActive = :active)")
    Page<User> search(@Param("keyword") String keyword,
                      @Param("role") RoleName role,
                      @Param("active") Boolean active,
                      Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(RoleName role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt >= CURRENT_DATE - 30")
    List<User> findNewUsers(RoleName role);

    @Query("SELECT u FROM User u JOIN u.examAttempts ea GROUP BY u.id ORDER BY AVG(ea.score) DESC")
    List<User> findTopUsers(org.springframework.data.domain.Pageable pageable);


    @Query("SELECT COUNT(uc) FROM UserCourse uc")
    long countTotal();

    @Query("SELECT COUNT(uc) FROM UserCourse uc WHERE uc.progressPercent = 100")
    long countCompleted();

    @Query("SELECT AVG(uc.progressPercent) FROM UserCourse uc")
    Double avgProgress();
}
