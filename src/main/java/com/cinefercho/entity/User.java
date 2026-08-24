package com.cinefercho.entity;

import com.cinefercho.entity.enums.MembershipType;
import com.cinefercho.entity.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_national_id", columnNames = "national_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{6,10}")
    @Size(min = 6, max = 10)
    @Column(name = "national_id", nullable = false, length = 10)
    private String nationalId;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false, length = 20)
    private MembershipType membershipType;

    @Column(name = "membership_expires_at")
    private Instant membershipExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (membershipType == null) {
            membershipType = MembershipType.NONE;
        }
        if (role == null) {
            role = UserRole.ROLE_CLIENT;
        }
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoice.setUser(this);
    }

    public boolean expireIfNeeded() {
        if (membershipType == null || membershipType == MembershipType.NONE) {
            return false;
        }
        if (membershipExpiresAt != null && Instant.now().isBefore(membershipExpiresAt)) {
            return false;
        }
        membershipType = MembershipType.NONE;
        membershipExpiresAt = null;
        return true;
    }

    public boolean hasActiveMembership() {
        return membershipType != null
                && membershipType != MembershipType.NONE
                && membershipExpiresAt != null
                && Instant.now().isBefore(membershipExpiresAt);
    }
}
