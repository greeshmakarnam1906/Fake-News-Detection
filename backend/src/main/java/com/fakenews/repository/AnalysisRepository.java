package com.fakenews.repository;

import com.fakenews.model.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    Page<Analysis> findByUserId(Long userId, Pageable pageable);

    Optional<Analysis> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndLabel(Long userId, String label);

    List<Analysis> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
