package com.exemple.backend.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue #9 — EF9/RG6 : l'etudiant relu consulte sa note et le commentaire,
 * sans jamais voir l'identite du relecteur. Le resultat est reserve a l'auteur.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResultatAnonymeTest {

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

	private Exercice exerciceRelu(long auteurId, long relecteurId, int note) {
		Session session = sessions.findById(1L).orElseThrow();
		presences.save(new Presence(session, auteurId, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		presences.save(new Presence(session, relecteurId, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		Exercice exercice = exercices.save(new Exercice(session, auteurId, "https://exemples.dev/exo", OffsetDateTime.now()));
		Relecture relecture = new Relecture(exercice, relecteurId, note, "Tres bon rendu.", OffsetDateTime.now());
		relectures.save(relecture);
		exercice.marquerRelue();
		return exercices.save(exercice);
	}

	@Test
	void auteurVoitNoteEtCommentaireMaisJamaisLeRelecteur_RG6() throws Exception {
		Exercice exercice = exerciceRelu(1L, 3L, 16);

		String corps = mockMvc.perform(get("/api/exercices/" + exercice.getId() + "/resultat")
						.header("X-Etudiant-Id", 1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.note").value(16))
				.andExpect(jsonPath("$.commentaire").value("Tres bon rendu."))
				.andExpect(jsonPath("$.relecteurId").doesNotExist())
				.andReturn().getResponse().getContentAsString();

		// Garantie RG6 : ni "relecteur", ni "relecteurId", ni identifiant 3 en clair de champ relecteur.
		assertThat0Relecteur(corps);
	}

	private static void assertThat0Relecteur(String corps) {
		if (corps.toLowerCase().contains("relecteur")) {
			throw new AssertionError("RG6 violated : identite du relecteur exposee -> " + corps);
		}
	}

	@Test
	void resultat404TantQueLaRelectureNestPasRendue() throws Exception {
		Session session = sessions.findById(1L).orElseThrow();
		presences.save(new Presence(session, 1L, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		presences.save(new Presence(session, 2L, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		Exercice exercice = exercices.save(new Exercice(session, 1L, "https://exemples.dev/exo2", OffsetDateTime.now()));
		relectures.save(new Relecture(exercice, 2L, 0, "", OffsetDateTime.now()));
		exercice.assignerRelecteur();
		exercices.save(exercice);

		mockMvc.perform(get("/api/exercices/" + exercice.getId() + "/resultat")
						.header("X-Etudiant-Id", 1))
				.andExpect(status().isNotFound());
	}

	@Test
	void resultatReserveAAuteur() throws Exception {
		Exercice exercice = exerciceRelu(1L, 3L, 11);

		mockMvc.perform(get("/api/exercices/" + exercice.getId() + "/resultat")
						.header("X-Etudiant-Id", 4))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("AUTEUR_NON_ELIGIBLE"));
	}

	@Test
	void consultationAvantRelectureRendue_404() throws Exception {
		// Exercice depose et assigne mais pas encore relu : l'auteur consulte, 404.
		Session session = sessions.findById(1L).orElseThrow();
		presences.save(new Presence(session, 2L, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		presences.save(new Presence(session, 3L, OffsetDateTime.now(), SourcePresence.ETUDIANT));
		Exercice exercice = exercices.save(new Exercice(session, 2L, "https://exemples.dev/flux", OffsetDateTime.now()));
		relectures.save(new Relecture(exercice, 3L, 0, "", OffsetDateTime.now()));
		exercice.assignerRelecteur();
		exercices.save(exercice);

		mockMvc.perform(get("/api/exercices/" + exercice.getId() + "/resultat")
						.header("X-Etudiant-Id", 2))
				.andExpect(status().isNotFound());
	}
}
