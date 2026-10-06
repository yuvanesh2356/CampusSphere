package com.campussphere.common.service;

import com.campussphere.common.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Handles saving and deleting uploaded files on the local filesystem.
 * Placed under common/service (not inside the marketplace package)
 * because Freelance, Guidance, and Lost & Found will all need the same
 * "save an uploaded image, get back a servable path" behavior in later
 * phases - this keeps that logic in one place instead of four.
 *
 * Files are stored outside the classpath (configurable via
 * campussphere.upload.dir) so uploads persist across application
 * restarts and are not bundled into the build artifact.
 */
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/jpg");

    @Value("${campussphere.upload.dir}")
    private String uploadDir;

    /**
     * Saves an uploaded file under uploadDir/subDirectory with a
     * generated unique filename (to avoid collisions and to avoid
     * trusting the client-supplied filename).
     *
     * @param file          the uploaded file (may be null/empty - caller decides if that's allowed)
     * @param subDirectory  logical grouping folder, e.g. "marketplace"
     * @return the relative path (e.g. "marketplace/&lt;uuid&gt;.jpg") to store on the entity,
     *         or null if no file was provided
     */
    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException("Only JPG and PNG images are allowed");
        }

        try {
            Path targetDir = Paths.get(uploadDir, subDirectory);
            Files.createDirectories(targetDir);

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
            String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
            String generatedName = UUID.randomUUID() + extension;

            Path targetPath = targetDir.resolve(generatedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + generatedName;
        } catch (IOException ex) {
            throw new InvalidFileException("Failed to store uploaded file: " + ex.getMessage());
        }
    }

    /**
     * Deletes a previously stored file. Safe to call with null or a
     * path that no longer exists - both are silently ignored, since a
     * missing file on delete is not an error condition worth surfacing.
     */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(uploadDir, relativePath));
        } catch (IOException ex) {
            // Deliberately not rethrown: a failed cleanup of an old file
            // should never block the primary operation (e.g. an update
            // or delete) that triggered it.
        }
    }
}
