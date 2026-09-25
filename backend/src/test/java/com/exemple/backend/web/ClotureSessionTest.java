package com.exemple.backend.web;

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
 * Issue #11 — EF12 : le formateur cloture la session. Apres cloture, plus
 * aucun depot ni relecture n'est possible (RG10). Distinction §7 du cahier
 * des charges : l'expiration du code (RG1/RG2) bloque la presence seulement,
 * le depot reste possible jusqu'a la cloture explicite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClotureSessionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SessionRepository sessions;

	@Test
	void cloture_renvoie200EtMarqueLaSession() throws Exception {
		mockMvc.perform(post("/api/sessions/1/cloture"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.cloturee").value(true));
	}

	@Test
	void depotApresCloture_refuse409_RG10() throws Exception {
		mockMvc.perform(post("/api/sessions/1/cloture")).andExpect(status().isOk());

		mockMvc.perform(post("/api/exercices")
						.header("X-Etudiant-Id", 6)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"sessionId\": 1, \"lien\": \"https://exemples.dev/trop-tard\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
	}

	@Test
	void presenceApresCloture_refuse409() throws Exception {
		mockMvc.perform(post("/api/sessions/1/cloture")).andExpect(status().isOk());

		mockMvc.perform(post("/api/presences")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\": \"DEMO1234\", \"etudiantId\": 7}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
	}

	@Test
	void depotRestePossibleApresExpirationDuCode_horsCloture() throws Exception {
		// §7 : "fin de session" (code expire, RG1) != "cloture" (EF12).
		// Le depot reste possible apres l'expiration du code de presence.
		OffsetDateTime passe = OffsetDateTime.now().minusMinutes(30);
		sessions.save(new Session(900L, "EXPRIRE", passe, passe.plusMinutes(15)));

		// La presence est refusee (code expire)...
		mockMvc.perform(post("/api/presences")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\": \"EXPRIRE\", \"etudiantId\": 8}"))
				.andExpect(status().isGone());

		// ...mais la session n'etant pas cloturee, le depot est accepte.
		Long idSession = sessions.findByCode("EXPRIRE").orElseThrow().getId();
		mockMvc.perform(post("/api/exercices")
						.header("X-Etudiant-Id", 8)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"sessionId\": " + idSession + ", \"lien\": \"https://exemples.dev/hors-delai\"}"))
				.andExpect(status().isCreated());
	}
}
