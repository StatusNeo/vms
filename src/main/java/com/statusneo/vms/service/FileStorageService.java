package com.statusneo.vms.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/";

    public FileStorageService() {
        // Create the uploads directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        // Generate a unique filename
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        // Save the file to the uploads directory
        Files.write(filePath, file.getBytes());

        return filePath.toString();
    }

    public String getUploadDir() {
        return uploadDir;
    }
}