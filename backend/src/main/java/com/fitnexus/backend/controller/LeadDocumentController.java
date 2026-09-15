package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.LeadDocument;
import com.fitnexus.backend.service.LeadDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadDocumentController {

    private final LeadDocumentService leadDocumentService;

    @PostMapping("/{leadId}/documents")
    public ResponseEntity<?> uploadDocument(@PathVariable Long leadId, @RequestParam("file") MultipartFile file) {
        try {
            LeadDocument doc = leadDocumentService.uploadDocument(leadId, file);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Document uploaded successfully",
                    doc,
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/leads/" + leadId + "/documents", e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading document for lead {}: {}", leadId, e.getMessage());
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads/" + leadId + "/documents", "Failed to upload document");
        }
    }

    @GetMapping("/{leadId}/documents")
    public ResponseEntity<?> getLeadDocuments(@PathVariable Long leadId) {
        try {
            List<LeadDocument> docs = leadDocumentService.getLeadDocuments(leadId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Documents retrieved successfully",
                    docs,
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving documents for lead {}: {}", leadId, e.getMessage());
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads/" + leadId + "/documents", "Failed to retrieve documents");
        }
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        try {
            LeadDocument docDetails = leadDocumentService.getDocumentDetails(documentId);
            Resource resource = leadDocumentService.loadDocumentAsResource(documentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(docDetails.getFileType() != null ? docDetails.getFileType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docDetails.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
                message,
                LocalDateTime.now().toString(),
                path
        ));
    }
}
