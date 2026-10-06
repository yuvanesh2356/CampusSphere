package com.campussphere.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * Incoming payload for the "Edit Profile" form. Deliberately does not
 * include email, password, or role - those are managed through
 * registration/authentication, not the profile editor. Department and
 * yearOfStudy remain editable here since they're genuinely profile
 * data, distinct from identity/security fields.
 */
public class ProfileUpdateDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotNull(message = "Year of study is required")
    @Min(value = 1, message = "Year of study must be between 1 and 4")
    @Max(value = 4, message = "Year of study must be between 1 and 4")
    private Integer yearOfStudy;

    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;

    @Size(max = 1000, message = "About Me must not exceed 1000 characters")
    private String aboutMe;

    @Size(max = 150, message = "Contact details must not exceed 150 characters")
    private String contactDetails;

    /** Optional replacement profile picture. Null/empty means "keep the current one". */
    private MultipartFile profilePicture;

    public ProfileUpdateDTO() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public MultipartFile getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(MultipartFile profilePicture) {
        this.profilePicture = profilePicture;
    }
}
