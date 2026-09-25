package com.exemple.backend.repository;

import com.exemple.backend.entity.Relecture;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

	Optional<Relecture> findByExerciceId(Long exerciceId);

	List<Relecture> findByRelecteurId(Long relecteurId);

	List<Relecture> findByExerciceSessionId(Long sessionId);
}
