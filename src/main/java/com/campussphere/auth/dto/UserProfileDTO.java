package com.campussphere.auth.dto;

import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;

import java.time.LocalDateTime;

/**
 * Outgoing representation of a User, returned by the API and used to
 * render profile information in views. Deliberately excludes the
 * password field - this is what prevents sensitive data from ever
 * leaking into an API response.
 */
public class UserProfileDTO {

    private Long id;
    private String fullName;
    private String email;
    private String department;
    private Integer yearOfStudy;
    private Role role;
    private boolean verified;
    private LocalDateTime createdAt;
    private String profilePictureUrl;
    private String skills;
    private String aboutMe;
    private String contactDetails;

    public UserProfileDTO() {
    }

    /**
     * Converts a User entity into its safe, external-facing representation.
     * Kept as a static factory here (rather than in the entity itself) to
     * keep the entity free of any web/DTO-layer concerns.
     */
    public static UserProfileDTO fromEntity(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setDepartment(user.getDepartment());
        dto.setYearOfStudy(user.getYearOfStudy());
        dto.setRole(user.getRole());
        dto.setVerified(user.isVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setProfilePictureUrl(user.getProfilePicturePath() != null ? "/uploads/" + user.getProfilePicturePath() : null);
        dto.setSkills(user.getSkills());
        dto.setAboutMe(user.getAboutMe());
        dto.setContactDetails(user.getContactDetails());
        return dto;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
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
}
