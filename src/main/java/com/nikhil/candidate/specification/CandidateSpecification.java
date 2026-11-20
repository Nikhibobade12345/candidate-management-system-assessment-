package com.nikhil.candidate.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.nikhil.candidate.dto.CandidateSearchCriteria;
import com.nikhil.candidate.entity.Candidate;

import jakarta.persistence.criteria.Predicate;

public class CandidateSpecification {

    private CandidateSpecification() {
        // Private constructor to prevent instantiation of utility class
    }

    private static String escapeWildcards(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
    }

    public static Specification<Candidate> build(CandidateSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            if (criteria == null) {
                return criteriaBuilder.conjunction(); // Return always-true predicate (no filtering)
            }

            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword search: matches firstName OR lastName OR email OR currentCompany
            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String escapedSearch = escapeWildcards(criteria.getSearch().trim().toLowerCase());
                String searchTerm = "%" + escapedSearch + "%";

                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchTerm, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchTerm, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchTerm, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("currentCompany")), searchTerm, '\\')
                );
                predicates.add(searchPredicate);
            }

            // 2. Exact filter by Status (AVAILABLE, HIRED, etc.)
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            // 3. Partial filter by Location (case-insensitive)
            if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
                String escapedLocation = escapeWildcards(criteria.getLocation().trim().toLowerCase());
                String locationPattern = "%" + escapedLocation + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")), locationPattern, '\\'));
            }

            // 4. Minimum Experience filter (experience >= minExperience)
            if (criteria.getMinExperience() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("experience"), criteria.getMinExperience()));
            }

            // 5. Maximum Experience filter (experience <= maxExperience)
            if (criteria.getMaxExperience() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("experience"), criteria.getMaxExperience()));
            }

            // 6. Salary Range filters
            if (criteria.getMinSalary() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("expectedSalary"), criteria.getMinSalary()));
            }

            if (criteria.getMaxSalary() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("expectedSalary"), criteria.getMaxSalary()));
            }

            // 7. Maximum Notice Period (noticePeriod <= maxNoticePeriod, e.g. candidates joining within 30 days)
            if (criteria.getMaxNoticePeriod() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("noticePeriod"), criteria.getMaxNoticePeriod()));
            }

            // Combine all active predicates with SQL "AND"
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}