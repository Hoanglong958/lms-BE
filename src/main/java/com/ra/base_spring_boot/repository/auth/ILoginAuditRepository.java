package com.ra.base_spring_boot.repository.auth;

import com.ra.base_spring_boot.model.LoginAudit;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILoginAuditRepository extends JpaRepository<LoginAudit, Long> {
    List<LoginAudit> findTop100ByRoleInOrderByLoginAtDesc(List<RoleName> roles);
    List<LoginAudit> findTop50ByUser_IdOrderByLoginAtDesc(Long userId);
}
