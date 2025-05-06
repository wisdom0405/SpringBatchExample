package com.example.SpringBatchExample.repository;

import com.example.SpringBatchExample.entity.WinEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WinRepository extends JpaRepository<WinEntity, Long> {

    // win 컬럼이 특정한 상수값보다 크거나 같을 경우
    Page<WinEntity> findByWinGreaterThanEqual(Long win, Pageable pageable);
}
