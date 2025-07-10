package com.smartfactory.smartmes_insight.domain.production;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionResultRepository extends JpaRepository<ProductionResult, Long> {
}