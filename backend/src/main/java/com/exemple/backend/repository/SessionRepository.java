package com.exemple.backend.repository;

import com.exemple.backend.entity.Session;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

	Optional<Session> findByCode(String code);
}
