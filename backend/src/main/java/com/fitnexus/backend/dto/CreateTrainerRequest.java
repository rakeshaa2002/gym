package com.fitnexus.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainerRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
            message = "Password must contain uppercase, lowercase, digit, and special character"
    )
    private String password;

    /** On update: when true the password field is ignored and the current one kept. */
    private Boolean keepPassword;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name should only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name should only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @NotBlank(message = "Specialization cannot be blank")
    @Size(min = 2, max = 100, message = "Specialization must be between 2 and 100 characters")
    private String specialization;

    @jakarta.validation.constraints.NotNull(message = "Experience years cannot be null")
    @jakarta.validation.constraints.Min(value = 0, message = "Experience years must be non-negative")
    @jakarta.validation.constraints.Max(value = 60, message = "Experience years cannot exceed 60")
    private Integer experienceYears;

    @NotBlank(message = "Certification cannot be blank")
    @Size(min = 2, max = 100, message = "Certification must be between 2 and 100 characters")
    private String certification;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid (10-15 digits, optional + prefix)")
    private String phone;

    @NotBlank(message = "Qualification cannot be blank")
    @Size(min = 2, max = 100, message = "Qualification must be between 2 and 100 characters")
    private String qualification;

    @jakarta.validation.constraints.NotNull(message = "Rate per hour cannot be null")
    @jakarta.validation.constraints.DecimalMin(value = "100.0", message = "Rate per hour must be at least 100")
    @jakarta.validation.constraints.DecimalMax(value = "10000.0", message = "Rate per hour cannot exceed 10000")
    private Double ratePerHour;

    private Long headOfficeId;
    private Long branchId;
    private Long departmentId;
    private Long teamId;
    private Long designationId;
    private String joinDate;
    private String bio;
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
    private String offerLetterDocumentPath;
    private String languages;
    private Double rating;
    private Integer totalClientsTrained;
    private Long reportsToId;
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
    private String declarationPlace;}






