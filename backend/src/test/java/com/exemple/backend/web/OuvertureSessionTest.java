package com.exemple.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exemple.backend.entity.Session;
import com.exemple.backend.repository.SessionRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue #5 — EF1 : le formateur ouvre une session et obtient un code.
 * RG1 : le code expire 15 minutes apres l'ouverture.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OuvertureSessionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SessionRepository sessions;

	@Test
	void ouverture_renvoie201AvecCodeEtExpirationA15Minutes() throws Exception {
		OffsetDateTime avant = OffsetDateTime.now().minusSeconds(1);

		String corps = mockMvc.perform(post("/api/sessions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"formateurId\": 900}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.code").isNotEmpty())
				.andExpect(jsonPath("$.cloturee").value(false))
				.andReturn().getResponse().getContentAsString();

		// RG1 : expiration = ouverture + 15 minutes (tolerance 5 s d'horloge).
		Number idNumero = com.jayway.jsonpath.JsonPath.read(corps, "$.id");
		Optional<Session> session = sessions.findById(idNumero.longValue());
		assertThat(session).isPresent();
		assertThat(session.get().getOuverture()).isAfter(avant);
		assertThat(Duration.between(session.get().getOuverture(), session.get().getExpirationCode()))
				.isCloseTo(Duration.ofMinutes(15), Duration.ofSeconds(5));
	}

	@Test
	void chaqueSessionRecoitUnCodeUnique() throws Exception {
		String corps1 = mockMvc.perform(post("/api/sessions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"formateurId\": 900}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String corps2 = mockMvc.perform(post("/api/sessions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"formateurId\": 900}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String code1 = com.jayway.jsonpath.JsonPath.read(corps1, "$.code");
		String code2 = com.jayway.jsonpath.JsonPath.read(corps2, "$.code");
		assertThat(code1).isNotEqualTo(code2);
	}
}
