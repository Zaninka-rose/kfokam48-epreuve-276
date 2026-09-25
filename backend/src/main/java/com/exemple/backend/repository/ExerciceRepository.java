package com.exemple.backend.repository;

import com.exemple.backend.entity.Exercice;
import com.exemple.backend.entity.StatutExercice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

	Optional<Exercice> findBySessionIdAndAuteurId(Long sessionId, Long auteurId);

	List<Exercice> findBySessionId(Long sessionId);

	List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);

	List<Exercice> findByStatut(StatutExercice statut);
}
