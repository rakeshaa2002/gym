package com.fitnexus.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trainers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users account;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "specialization", nullable = false, length = 100)
    private String specialization;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Column(name = "certification", nullable = false, length = 100)
    private String certification;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "qualification", nullable = false, length = 100)
    private String qualification;

    @Column(name = "rate_per_hour", nullable = false)
    private Double ratePerHour;

    @OneToMany(mappedBy = "assignedTrainer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<FitnessUser> assignedUsers;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "languages", length = 200)
    private String languages;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_clients_trained")
    private Integer totalClientsTrained = 0;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 30)
    private String gender;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "personal_email", length = 100)
    private String personalEmail;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(name = "current_address", length = 300)
    private String currentAddress;

    @Column(name = "permanent_address", length = 300)
    private String permanentAddress;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "state", length = 80)
    private String state;

    @Column(name = "pincode", length = 20)
    private String pincode;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "work_location", length = 100)
    private String workLocation;

    @Column(name = "reporting_manager_name", length = 100)
    private String reportingManagerName;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 40)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc_code", length = 20)
    private String bankIfscCode;

    @Column(name = "bank_account_type", length = 30)
    private String bankAccountType;

    @Column(name = "qualification_document_path", length = 500)
    private String qualificationDocumentPath;

    @Column(name = "certification_document_path", length = 500)
    private String certificationDocumentPath;

    @Column(name = "id_proof_document_path", length = 500)
    private String idProofDocumentPath;

    @Column(name = "address_proof_document_path", length = 500)
    private String addressProofDocumentPath;

    @Column(name = "resume_document_path", length = 500)
    private String resumeDocumentPath;

    @Column(name = "offer_letter_document_path", length = 500)
    private String offerLetterDocumentPath;


    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "marital_status", length = 30)
    private String maritalStatus;

    @Column(name = "spouse_name", length = 100)
    private String spouseName;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "candidate_photo_path", length = 500)
    private String candidatePhotoPath;

    @Column(name = "aadhar_card_document_path", length = 500)
    private String aadharCardDocumentPath;

    @Column(name = "pan_card_document_path", length = 500)
    private String panCardDocumentPath;

    @Column(name = "bank_document_path", length = 500)
    private String bankDocumentPath;

    @Column(name = "bank_account_holder_name", length = 100)
    private String bankAccountHolderName;

    @Column(name = "bank_branch", length = 150)
    private String bankBranch;

    @Column(name = "previous_employment_1", length = 500)
    private String previousEmployment1;

    @Column(name = "previous_employment_2", length = 500)
    private String previousEmployment2;

    @Column(name = "experience_certificate_document_path", length = 500)
    private String experienceCertificateDocumentPath;
    @Column(name = "course_certificate_path", length = 500)
    private String courseCertificatePath;

    @Column(name = "education_certificate_path", length = 500)
    private String educationCertificatePath;

    @Column(name = "emergency_contact_relationship", length = 80)
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_name_2", length = 100)
    private String emergencyContactName2;

    @Column(name = "emergency_contact_relationship_2", length = 80)
    private String emergencyContactRelationship2;

    @Column(name = "emergency_phone_2", length = 20)
    private String emergencyPhone2;

    @Column(name = "reference_name_1", length = 100)
    private String referenceName1;

    @Column(name = "reference_phone_1", length = 20)
    private String referencePhone1;

    @Column(name = "reference_name_2", length = 100)
    private String referenceName2;

    @Column(name = "reference_phone_2", length = 20)
    private String referencePhone2;

    @Column(name = "joining_branch_name", length = 100)
    private String joiningBranchName;

    @Column(name = "source_platform", length = 80)
    private String sourcePlatform;

    @Column(name = "pf_uan", length = 30)
    private String pfUan;

    @Column(name = "esi_number", length = 30)
    private String esiNumber;

    @Column(name = "declaration_date")
    private LocalDate declarationDate;

    @Column(name = "declaration_place", length = 100)
    private String declarationPlace;
    @PrePersist
    protected void onCreate() {
        if (joinDate == null) {
            joinDate = LocalDateTime.now();
        }
    }
}







