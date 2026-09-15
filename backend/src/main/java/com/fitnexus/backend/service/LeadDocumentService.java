package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.Lead;
import com.fitnexus.backend.entity.LeadDocument;
import com.fitnexus.backend.repository.LeadDocumentRepository;
import com.fitnexus.backend.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadDocumentService {

    private final LeadDocumentRepository leadDocumentRepository;
    private final LeadRepository leadRepository;

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    @Transactional
    public LeadDocument uploadDocument(Long leadId, MultipartFile file) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Generate unique filename to avoid overwriting
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            LeadDocument doc = new LeadDocument();
            doc.setLead(lead);
            doc.setFileName(originalFileName);
            doc.setFileType(file.getContentType());
            doc.setUploadPath(uniqueFileName); // Just store the relative name
            doc.setUploadDate(LocalDateTime.now());

            return leadDocumentRepository.save(doc);
        } catch (IOException ex) {
            log.error("Could not store file", ex);
            throw new RuntimeException("Could not store file", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<LeadDocument> getLeadDocuments(Long leadId) {
        return leadDocumentRepository.findByLeadIdOrderByUploadDateDesc(leadId);
    }

    @Transactional(readOnly = true)
    public Resource loadDocumentAsResource(Long documentId) {
        LeadDocument doc = leadDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(doc.getUploadPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found", ex);
        }
    }

    @Transactional(readOnly = true)
    public LeadDocument getDocumentDetails(Long documentId) {
        return leadDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }
}
