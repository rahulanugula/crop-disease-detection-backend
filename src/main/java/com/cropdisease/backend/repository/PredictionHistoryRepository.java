package com.cropdisease.backend.repository;

import com.cropdisease.backend.model.PredictionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionHistoryRepository extends JpaRepository<PredictionHistory, Long> {
    List<PredictionHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
