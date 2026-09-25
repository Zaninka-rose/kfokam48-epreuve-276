package com.exemple.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exemple.backend.entity.Session;
import com.exemple.backend.repository.SessionRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Critères d'acceptation de l'issue "L'etudiant marque sa presence avec un code" :
 * - code valide et non expire -> presence visible dans le tableau du formateur ;
 * - code de plus de 15 minutes -> 410 CODE_EXPIRE (RG1/RG2) ;
 * - presence deja marquee -> 409 DEJA_PRESENT (EF4).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SessionRepository sessions;

	private String corpsPresence(String code, long etudiantId) {
		return "{\"code\": \"" + code + "\", \"etudiantId\": " + etudiantId + "}";
	}

	@Test
	void codeValide_creeLaPresenceVisibleDansLeTableau() throws Exception {
		Session session = sessions.findById(1L).orElseThrow();
		assertThat(session.isCloturee()).isFalse();		mockMvc.perform(post("/api/presences")
					.contentType(MediaType.APPLICATION_JSON)
					.content(corpsPresence("DEMO1234", 1)))
			.andExpect(status().isCreated())
				.andExpect(jsonPath("$.etudiantId").value(1))
				.andExpect(jsonPath("$.source").value("ETUDIANT"));

		// La présence apparaît dans le tableau du formateur (EF10).
		mockMvc.perform(get("/api/sessions/1/tableau"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].etudiantId").value(1))
				.andExpect(jsonPath("$[0].presences").value(1));
	}

	@Test
	void codeExpire_renvoie410CodeExpire() throws Exception {
		// Session dont le code a expiré depuis plus de 15 minutes (RG1/RG2).
		OffsetDateTime passe = OffsetDateTime.now().minusMinutes(30);
		Session expiree = sessions.save(new Session(900L, "EXPIRE1", passe, passe.plusMinutes(15)));		mockMvc.perform(post("/api/presences")
					.contentType(MediaType.APPLICATION_JSON)
					.content(corpsPresence("EXPIRE1", 2)))
			.andExpect(status().isGone())
				.andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
				.andExpect(jsonPath("$.status").value(410));
	}	@Test
	void doublePresence_renvoie409DejaPresent() throws Exception {
		mockMvc.perform(post("/api/presences")
					.contentType(MediaType.APPLICATION_JSON)
					.content(corpsPresence("DEMO1234", 3)))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/presences")
					.contentType(MediaType.APPLICATION_JSON)
					.content(corpsPresence("DEMO1234", 3)))
			.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DEJA_PRESENT"))
				.andExpect(jsonPath("$.status").value(409));
	}
}
