package com.campussphere.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Core identity entity for CampusSphere. Every content module
 * (MarketplaceListing, FreelanceService, GuidancePost, LostFoundPost)
 * relates back to this entity as the owner/author of a post, and the
 * Notification and Admin modules also operate on it directly.
 *
 * Extended in the Final Phase with optional profile fields
 * (profilePicturePath, skills, aboutMe, contactDetails) on top of the
 * Phase 1 authentication fields - all additive and nullable, so no
 * existing registration/login/module code needed to change.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false)
    private Integer yearOfStudy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.STUDENT;

    /**
     * Whether the user has completed college-email verification.
     * Set to true immediately at registration for Phase 1 (domain-based
     * check only). A full email-confirmation-link workflow can be
     * layered on top of this flag in a later phase without any schema change.
     */
    @Column(nullable = false)
    private boolean verified = false;

    /**
     * Profile fields added in the Final Phase. All nullable and
     * additive - existing registration/login flow is completely
     * unaffected, since none of these are collected at registration.
     * A user has none of these set until they visit Edit Profile.
     */
    @Column(length = 255)
    private String profilePicturePath;

    @Column(length = 500)
    private String skills;

    @Column(length = 1000)
    private String aboutMe;

    @Column(length = 150)
    private String contactDetails;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String fullName, String email, String password, String department, Integer yearOfStudy) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
    }

    // ---------- Getters and Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
