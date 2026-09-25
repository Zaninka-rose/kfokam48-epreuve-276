package com.exemple.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exemple.backend.entity.Exercice;
import com.exemple.backend.entity.Presence;
import com.exemple.backend.entity.Relecture;
import com.exemple.backend.entity.Session;
import com.exemple.backend.entity.SourcePresence;
import com.exemple.backend.entity.StatutExercice;
import com.exemple.backend.repository.ExerciceRepository;
import com.exemple.backend.repository.PresenceRepository;
import com.exemple.backend.repository.RelectureRepository;
import com.exemple.backend.repository.SessionRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue #8 — EF8 : le relecteur designe envoie note (RG7 : entier 0-20) et
 * commentaire. Refus si l'appelant est l'auteur (RG4, 403) ou si la relecture
 * est deja rendue — elle est definitive (RG8, 409).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RelectureNoteTest {

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

	private Exercice exerciceAssigné(long auteurId, long relecteurId) {
		Session session = sessions.findById(1L).orElseThrow();
		presences.save(new Presence(session, auteurId, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		presences.save(new Presence(session, relecteurId, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		Exercice exercice = exercices.save(new Exercice(session, auteurId, "https://exemples.dev/exo", OffsetDateTime.now()));
		Relecture assignation = new Relecture(exercice, relecteurId, 0, "", OffsetDateTime.now());
		relectures.save(assignation);
		exercice.assignerRelecteur();
		return exercices.save(exercice);
	}

	private String corps(int note, String commentaire) {
		return "{\"note\": " + note + ", \"commentaire\": \"" + commentaire + "\"}";
	}

	@Test
	void relectureValide_renvoie200EtPasseExerciceEnRelue() throws Exception {
		Exercice exercice = exerciceAssigné(1L, 2L);

		mockMvc.perform(post("/api/relectures/" + exercice.getId())
						.header("X-Relecteur-Id", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corps(15, "Bon travail, argumentation claire.")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.note").value(15))
				.andExpect(jsonPath("$.commentaire").value("Bon travail, argumentation claire."));

		assertThat(exercices.findById(exercice.getId()).orElseThrow().getStatut())
				.isEqualTo(StatutExercice.RELUE);
	}

	@Test
	void noteHorsBornes_refuse400_RG7() throws Exception {
		Exercice exercice = exerciceAssigné(1L, 2L);

		mockMvc.perform(post("/api/relectures/" + exercice.getId())
						.header("X-Relecteur-Id", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corps(21, "Trop genereux")))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/relectures/" + exercice.getId())
						.header("X-Relecteur-Id", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corps(-1, "Note negative")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void auteurQuiRelireSonExercice_refuse403_RG4() throws Exception {
		Exercice exercice = exerciceAssigné(1L, 2L);

		mockMvc.perform(post("/api/relectures/" + exercice.getId())
						.header("X-Relecteur-Id", 1)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corps(18, "Je me note moi-meme")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("AUTEUR_NON_ELIGIBLE"));
	}

	@Test
	void secondeRelecture_refuse409Definitive_RG8() throws Exception {
		Exercice exercice = exerciceAssigné(1L, 2L);

		mockMvc.perform(post("/api/relectures/" + exercice.getId())
						.header("X-Relecteur-Id", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corps(12, "Premiere version")))
				.andExpect(status().isOk());

		// RG8 : la relecture rendue est definitive, aucun second envoi.
		mockMvc.perform(post("/api/relectures/" + exercice.getId())
						.header("X-Relecteur-Id", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corps(14, "Je change d'avis")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
	}
}
