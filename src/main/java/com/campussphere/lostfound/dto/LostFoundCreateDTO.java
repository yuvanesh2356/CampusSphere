package com.campussphere.lostfound.dto;

import com.campussphere.lostfound.entity.ItemCategory;
import com.campussphere.lostfound.entity.PostType;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Incoming payload for the "Report Lost/Found Item" form. Bound via
 * @ModelAttribute, same multipart/form-data pattern every other
 * module's create DTO uses.
 */
public class LostFoundCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private ItemCategory category;

    @NotNull(message = "Post type is required")
    private PostType postType;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @NotNull(message = "Date lost/found is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateLostOrFound;

    @NotBlank(message = "Contact information is required")
    @Size(max = 100, message = "Contact information must not exceed 100 characters")
    private String contactInformation;

    /** Optional - a post can be created without an image. */
    private MultipartFile image;

    public LostFoundCreateDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDateLostOrFound() {
        return dateLostOrFound;
    }

    public void setDateLostOrFound(LocalDate dateLostOrFound) {
        this.dateLostOrFound = dateLostOrFound;
    }

    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
