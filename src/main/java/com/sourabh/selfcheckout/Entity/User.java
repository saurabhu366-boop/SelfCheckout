package com.sourabh.selfcheckout.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// ✅ FIX Bug 3: Replaced @Data with specific Lombok annotations.
//    @Data on a JPA entity generates equals()/hashCode() using ALL fields.
//    Since `id` is null before save, this breaks entity comparisons in Sets/Maps.
//    @EqualsAndHashCode(of = "id") ensures only the DB primary key is used.
//
// ✅ FIX Bug 4: Renamed `isActive` → `active`.
//    A Boolean field named `isActive` makes Lombok generate `isActive()` (stripping "is"),
//    which conflicts with the JPA column name and causes JSON to serialize as "active"
//    instead of "isActive", breaking deserialization on both ends.
//
// ✅ FIX Bug 5: Timestamps moved to @PrePersist / @PreUpdate lifecycle callbacks.
//    LocalDateTime.now() in a field initializer is called once at class-load time,
//    not at entity creation. Also, updatedAt was never actually updated on changes.

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // ✅ Bug 4 fix: renamed from isActive to active
    @Column(nullable = false)
    private Boolean active = true;

    // ✅ Bug 5 fix: no longer initialized here — set via @PrePersist
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ✅ Bug 5 fix: lifecycle callbacks set timestamps correctly at runtime
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── UserDetails implementation ────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // ✅ Bug 4 fix: now uses renamed field `active`
        return active;
    }

    public enum Role {
        USER, ADMIN
    }
}