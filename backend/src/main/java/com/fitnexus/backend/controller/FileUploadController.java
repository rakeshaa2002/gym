package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png", "webp");
    private static final Path UPLOAD_ROOT = Paths.get(System.getProperty("user.dir"), "uploads", "employee-documents");

    @PostMapping("/employee-documents")
    public ResponseEntity<?> uploadEmployeeDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return badRequest("Please choose a file to upload");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                return badRequest("File size must be 10 MB or less");
            }

            String originalName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
            String extension = getExtension(originalName);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return badRequest("Only PDF, JPG, JPEG, PNG and WEBP files are allowed");
            }

            Files.createDirectories(UPLOAD_ROOT);
            String fileName = UUID.randomUUID() + "." + extension;
            Path target = UPLOAD_ROOT.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            FileUploadResponse response = new FileUploadResponse(
                    fileName,
                    originalName,
                    "/uploads/employee-documents/" + fileName,
                    file.getContentType(),
                    file.getSize()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(),
                    "File uploaded successfully",
                    response,
                    LocalDateTime.now().toString()
            ));
        } catch (IOException e) {
            log.error("Failed to upload employee document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Could not store uploaded file",
                    LocalDateTime.now().toString(),
                    "/api/uploads/employee-documents"
            ));
        }
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now().toString(),
                "/api/uploads/employee-documents"
        ));
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
