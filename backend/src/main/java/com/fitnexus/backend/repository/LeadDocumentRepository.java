package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.LeadDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadDocumentRepository extends JpaRepository<LeadDocument, Long> {
    List<LeadDocument> findByLeadIdOrderByUploadDateDesc(Long leadId);
}
