package com.ra.base_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Company_Profile")
public class CompanyProfile {
    @Id
    @Column(name = "company_id")
    private Integer companyId; // FK to Users.user_id and also PK

    @OneToOne
    @JoinColumn(name = "company_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private com.ra.base_spring_boot.entity.User company;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "industry", length = 100)
    private String industry;
}
