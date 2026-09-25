package com.exemple.backend.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exemple.backend.entity.Exercice;
import com.exemple.backend.entity.Presence;
import com.exemple.backend.entity.Relecture;
import com.exemple.backend.entity.Session;
import com.exemple.backend.entity.SourcePresence;
import com.exemple.backend.repository.ExerciceRepository;
import com.exemple.backend.repository.PresenceRepository;
import com.exemple.backend.repository.RelectureRepository;
import com.exemple.backend.repository.SessionRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue #10 — EF10 : le formateur consulte le tableau de bord. Pour chaque
 * etudiant : presences, exercices depose, moyenne des notes recues,
 * relectures en attente. Le relecteurId est visible ici (formateur) — RG6
 * ne l'interdit que dans les reponses de l'etudiant.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TableauFormateurTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SessionRepository sessions;

	@Autowired
	private PresenceRepository presences;

	@Autowired
	private ExerciceRepository exercices;

	@Autowired
	private RelectureRepository relectures;

	@Test
	void tableauVideQuandPersonneNEstPresent() throws Exception {
		mockMvc.perform(get("/api/sessions/1/tableau"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void tableauCompteLesPresencesParEtudiant() throws Exception {
		Session session = sessions.findById(1L).orElseThrow();
		presences.save(new Presence(session, 1L, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		presences.save(new Presence(session, 2L, OffsetDateTime.now(), SourcePresence.FORMATEUR));

		mockMvc.perform(get("/api/sessions/1/tableau"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].presences").value(1))
				.andExpect(jsonPath("$[1].presences").value(1))
				// RG12 : la source est visible pour le formateur (FORMATEUR != ETUDIANT).
				.andExpect(jsonPath("$[*].exercices").exists());
	}

	@Test
	void tableauMontreExerciceMoyenneEtRelecturesEnAttente() throws Exception {
		Session session = sessions.findById(1L).orElseThrow();
		presences.save(new Presence(session, 1L, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		presences.save(new Presence(session, 2L, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		presences.save(new Presence(session, 3L, OffsetDateTime.now(), SourcePresence.ETUDIANT));

		// Exercice de l'etudiant 1, relu par l'etudiant 2 : moyenne 15.
		Exercice relu = exercices.save(new Exercice(session, 1L, "https://exemples.dev/relu", OffsetDateTime.now()));
		relectures.save(new Relecture(relu, 2L, 15, "Bien.", OffsetDateTime.now()));
		relu.marquerRelue();
		exercices.save(relu);

		// Exercice de l'etudiant 2, assigne a l'etudiant 3 mais pas rendu : en attente.
		Exercice enAttente = exercices.save(new Exercice(session, 2L, "https://exemples.dev/attente", OffsetDateTime.now()));
		relectures.save(new Relecture(enAttente, 3L, 0, "", OffsetDateTime.now()));
		enAttente.assignerRelecteur();
		exercices.save(enAttente);

		mockMvc.perform(get("/api/sessions/1/tableau"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				// Etudiant 1 : exercice relu, moyenne 15, aucune relecture en attente pour lui.
				.andExpect(jsonPath("$[0].etudiantId").value(1))
				.andExpect(jsonPath("$[0].moyenne").value(15.0))
				.andExpect(jsonPath("$[0].relecturesEnAttente").value(0))
				// Etudiant 2 : exercice en attente de relecture, pas encore de moyenne.
				.andExpect(jsonPath("$[1].etudiantId").value(2))
				.andExpect(jsonPath("$[1].moyenne").doesNotExist())
				.andExpect(jsonPath("$[1].exercices[0].statut").value("EN_ATTENTE_RELECTURE"))
				// RG6 cote formateur : le relecteurId est visible dans le tableau.
				.andExpect(jsonPath("$[1].exercices[0].relecteurId").value(3))
				// Etudiant 3 : rien depose, pas de moyenne.
				.andExpect(jsonPath("$[2].etudiantId").value(3));
	}

	@Test
	void tableauDUneSessionInconnue_404() throws Exception {
		mockMvc.perform(get("/api/sessions/9999/tableau"))
				.andExpect(status().isNotFound());
	}
}
