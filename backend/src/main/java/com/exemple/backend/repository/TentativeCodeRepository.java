package com.exemple.backend.repository;

import com.exemple.backend.domain.TentativeCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativeCodeRepository extends JpaRepository<TentativeCode, Long> {
}
