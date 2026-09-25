package com.exemple.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Verifie que les donnees de demonstration sont chargees automatiquement
 * (migration V2) : 1 promotion, 10 etudiants, 1 session ouverte dont le code
 * est encore valide. L'application doit etre utilisable des le premier
 * demarrage, sans action manuelle.
 */
@SpringBootTest
class SeedDataTest {

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void laPromotionEtLesDixEtudiantsSontCharges() {
		Long promotions = jdbc.queryForObject("SELECT COUNT(*) FROM promotion", Long.class);
		Long etudiants = jdbc.queryForObject("SELECT COUNT(*) FROM etudiant", Long.class);
		assertThat(promotions).isEqualTo(1L);
		assertThat(etudiants).isEqualTo(10L);
	}

	@Test
	void laSessionDeDemoEstOuverteAvecUnCodeValide() {
		var lignes = jdbc.queryForList(
				"SELECT code, cloturee, expiration_code FROM session WHERE id = 1");
		assertThat(lignes).hasSize(1);

		var session = lignes.get(0);
		assertThat(session.get("code")).isEqualTo("DEMO1234");
		assertThat((Boolean) session.get("cloturee")).isFalse();

		java.time.OffsetDateTime expiration = (java.time.OffsetDateTime) session.get("expiration_code");
		assertThat(expiration.toInstant()).isAfter(OffsetDateTime.now().toInstant());
	}
}
