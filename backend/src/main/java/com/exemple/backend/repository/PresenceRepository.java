package com.exemple.backend.repository;

import com.exemple.backend.domain.Presence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

	Optional<Presence> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

	List<Presence> findBySessionId(Long sessionId);

	long countBySessionId(Long sessionId);
}
