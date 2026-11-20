package com.nikhil.candidate.dto;

import java.math.BigDecimal;

import com.nikhil.candidate.entity.CandidateStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;



public class CandidateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Pattern(
            regexp = "^[+]?[0-9\\s()-]{7,20}$",
            message = "Please provide a valid phone number"
    )
    private String phone;

    @NotNull(message = "Experience is required")
    @DecimalMin(value = "0.0", message = "Experience cannot be negative")
    @Digits(integer = 3, fraction = 1,
            message = "Experience must have at most 3 integer digits and 1 decimal place")
    private BigDecimal experience;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @Size(max = 150, message = "Current company cannot exceed 150 characters")
    private String currentCompany;

    @DecimalMin(value = "0.0", message = "Expected salary cannot be negative")
    @Digits(integer = 10, fraction = 2,
            message = "Expected salary must have at most 10 integer digits and 2 decimal places")
    private BigDecimal expectedSalary;

    @Min(value = 0, message = "Notice period cannot be negative")
    @Max(value = 365, message = "Notice period cannot exceed 365 days")
    private Integer noticePeriod;

    @NotNull(message = "Candidate status is required")
    private CandidateStatus status;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getExperience() {
        return experience;
    }

    public void setExperience(BigDecimal experience) {
        this.experience = experience;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCurrentCompany() {
        return currentCompany;
    }

    public void setCurrentCompany(String currentCompany) {
        this.currentCompany = currentCompany;
    }

    public BigDecimal getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(BigDecimal expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public Integer getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(Integer noticePeriod) {
        this.noticePeriod = noticePeriod;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }
}