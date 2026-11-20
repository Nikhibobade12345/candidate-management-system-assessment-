package com.nikhil.candidate.dto;

import java.math.BigDecimal;
import com.nikhil.candidate.entity.CandidateStatus;



public class CandidateSearchCriteria {

    // General keyword search (matches firstName, lastName, email, or currentCompany)
    private String search;

    // Specific filters (all optional)
    private CandidateStatus status;
    private String location;
    private BigDecimal minExperience;
    private BigDecimal maxExperience;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private Integer maxNoticePeriod;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(BigDecimal minExperience) {
        this.minExperience = minExperience;
    }

    public BigDecimal getMaxExperience() {
        return maxExperience;
    }

    public void setMaxExperience(BigDecimal maxExperience) {
        this.maxExperience = maxExperience;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.maxSalary = maxSalary;
    }

    public Integer getMaxNoticePeriod() {
        return maxNoticePeriod;
    }

    public void setMaxNoticePeriod(Integer maxNoticePeriod) {
        this.maxNoticePeriod = maxNoticePeriod;
    }
}