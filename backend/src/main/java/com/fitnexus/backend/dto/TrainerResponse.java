package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String specialization;
    private Integer experienceYears;
    private String certification;
    private String phone;
    private String qualification;
    private Double ratePerHour;
    private Boolean isActive;
    private String createdByName;
    private Long headOfficeId;
    private Long branchId;
    private Long departmentId;
    private Long teamId;
    private Long designationId;
    private LocalDateTime joinDate;
    private String bio;
    private String languages;
    private Double rating;
    private Integer totalClientsTrained;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String personalEmail;
    private String alternatePhone;
    private String emergencyContact;
    private String emergencyPhone;
    private String currentAddress;
    private String permanentAddress;
    private String city;
    private String state;
    private String pincode;
    private String employmentType;
    private String workLocation;
    private String reportingManagerName;
    private String probationEndDate;
    private String panNumber;
    private String aadharNumber;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankAccountType;
    private String qualificationDocumentPath;
    private String certificationDocumentPath;
    private String idProofDocumentPath;
    private String addressProofDocumentPath;
    private String resumeDocumentPath;
    private String offerLetterDocumentPath;    private Long reportsToId;
    private String reportsToName;
    private String reportsToRole;
    private String fatherName;
    private String motherName;
    private String maritalStatus;
    private String spouseName;
    private String location;
    private String candidatePhotoPath;
    private String aadharCardDocumentPath;
    private String panCardDocumentPath;
    private String bankDocumentPath;
    private String bankAccountHolderName;
    private String bankBranch;
    private String previousEmployment1;
    private String previousEmployment2;
    private String experienceCertificateDocumentPath;

    private String courseCertificatePath;
    private String educationCertificatePath;
    private String emergencyContactRelationship;
    private String emergencyContactName2;
    private String emergencyContactRelationship2;
    private String emergencyPhone2;
    private String referenceName1;
    private String referencePhone1;
    private String referenceName2;
    private String referencePhone2;
    private String joiningBranchName;
    private String sourcePlatform;
    private String pfUan;
    private String esiNumber;
    private String declarationDate;
    private String declarationPlace;    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TrainerResponse(Long id, String email, String firstName, String lastName, String specialization,
                           Integer experienceYears, String certification, String phone, String qualification,
                           Double ratePerHour, Boolean isActive, String createdByName, Long headOfficeId,
                           Long branchId, Long departmentId, Long teamId, Long designationId, LocalDateTime joinDate,
                           String bio, String languages, Double rating, Integer totalClientsTrained,
                           String dateOfBirth, String gender, String bloodGroup, String personalEmail,
                           String alternatePhone, String emergencyContact, String emergencyPhone,
                           String currentAddress, String permanentAddress, String city, String state, String pincode,
                           String employmentType, String workLocation, String reportingManagerName,
                           String probationEndDate, String panNumber, String aadharNumber, String bankName,
                           String bankAccountNumber, String bankIfscCode, String bankAccountType,
                           String qualificationDocumentPath, String certificationDocumentPath, String idProofDocumentPath,
                           String addressProofDocumentPath, String resumeDocumentPath, String offerLetterDocumentPath,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.certification = certification;
        this.phone = phone;
        this.qualification = qualification;
        this.ratePerHour = ratePerHour;
        this.isActive = isActive;
        this.createdByName = createdByName;
        this.headOfficeId = headOfficeId;
        this.branchId = branchId;
        this.departmentId = departmentId;
        this.teamId = teamId;
        this.designationId = designationId;
        this.joinDate = joinDate;
        this.bio = bio;
        this.languages = languages;
        this.rating = rating;
        this.totalClientsTrained = totalClientsTrained;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.personalEmail = personalEmail;
        this.alternatePhone = alternatePhone;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
        this.currentAddress = currentAddress;
        this.permanentAddress = permanentAddress;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.employmentType = employmentType;
        this.workLocation = workLocation;
        this.reportingManagerName = reportingManagerName;
        this.probationEndDate = probationEndDate;
        this.panNumber = panNumber;
        this.aadharNumber = aadharNumber;
        this.bankName = bankName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankIfscCode = bankIfscCode;
        this.bankAccountType = bankAccountType;
        this.qualificationDocumentPath = qualificationDocumentPath;
        this.certificationDocumentPath = certificationDocumentPath;
        this.idProofDocumentPath = idProofDocumentPath;
        this.addressProofDocumentPath = addressProofDocumentPath;
        this.resumeDocumentPath = resumeDocumentPath;
        this.offerLetterDocumentPath = offerLetterDocumentPath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }}






