package com.exemple.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exemple.backend.entity.Session;
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
 * Issue #6 — EF5 : l'etudiant depose le lien de son exercice.
 * Statut initial EN_ATTENTE_ASSIGNATION ou EN_ATTENTE_RELECTURE (EF5/RG9),
 * un seul exercice par etudiant et par session, refus si session cloturee (RG10).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepotExerciceTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SessionRepository sessions;

	private static final String CORPS = "{\"sessionId\": 1, \"lien\": \"https://exemples.dev/exo-awa\"}";

	@Test
	void depot_renvoie201AvecStatutInitial() throws Exception {
		String corps = mockMvc.perform(post("/api/exercices")
						.header("X-Etudiant-Id", 1)
						.contentType(MediaType.APPLICATION_JSON)
						.content(CORPS))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.auteurId").value(1))
				.andExpect(jsonPath("$.statut").value("EN_ATTENTE_ASSIGNATION"))
				.andReturn().getResponse().getContentAsString();

		// RG13 : s'il n'y a qu'un present (l'auteur), aucun relecteur eligible,
		// l'exercice reste en attente d'assignation.
		Number id = com.jayway.jsonpath.JsonPath.read(corps, "$.id");
		assertThat(id.longValue()).isPositive();
	}

	@Test
	void secondDepotMemeEtudiantMemeSession_refuse409() throws Exception {
		mockMvc.perform(post("/api/exercices")
						.header("X-Etudiant-Id", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.content(CORPS.replace("\"sessionId\": 1", "\"sessionId\": 1").replace("exo-awa", "exo-bruno")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/exercices")
						.header("X-Etudiant-Id", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.content(CORPS.replace("exo-awa", "exo-bruno-v2")))
				.andExpect(status().isConflict());
	}

	@Test
	void depotSurSessionCloturee_refuse409() throws Exception {
		Session session = sessions.findById(1L).orElseThrow();
		session.cloturer();
		sessions.save(session);

		mockMvc.perform(post("/api/exercices")
						.header("X-Etudiant-Id", 4)
						.contentType(MediaType.APPLICATION_JSON)
						.content(CORPS))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
	}

	@Test
	void depotSansEtudiant_refuse400() throws Exception {
		mockMvc.perform(post("/api/exercices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(CORPS))
				.andExpect(status().isBadRequest());
	}
}
