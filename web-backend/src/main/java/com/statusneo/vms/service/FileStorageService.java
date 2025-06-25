package com.statusneo.vms.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for handling file storage operations in the Visitor Management System.
 * Manages file uploads, storage, and retrieval of visitor-related files.
 *
 * @deprecated This class is scheduled for removal as it is not needed.
 */
@Deprecated(since = "1.0", forRemoval = true)
@Service
public class FileStorageService {

    // Directory where uploaded files will be stored
    private final String uploadDir = "uploads/";

    /**
     * Constructor that ensures the upload directory exists.
     * Creates the directory if it doesn't exist.
     */
    public FileStorageService() {
        // Create the uploads directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Stores an uploaded file in the system.
     * Process:
     * 1. Generates a unique filename using UUID to prevent collisions
     * 2. Combines the UUID with original filename for traceability
     * 3. Saves the file to the uploads directory
     *
     * @param file The file to be stored
     * @return The path where the file was stored
     * @throws IOException if file operations fail
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Generate a unique filename using UUID to prevent name collisions
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        // Create the full file path
        Path filePath = Paths.get(uploadDir + fileName);

        // Save the file to the uploads directory
        Files.write(filePath, file.getBytes());

        return filePath.toString();
    }

    /**
     * Returns the upload directory path.
     *
     * @return The path to the upload directory
     */
    public String getUploadDir() {
        return uploadDir;
    }
}