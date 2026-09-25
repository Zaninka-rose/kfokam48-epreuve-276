package com.exemple.backend.web;

import com.exemple.backend.entity.Presence;
import com.exemple.backend.entity.Session;
import com.exemple.backend.service.RelectureService;
import com.exemple.backend.service.SessionService;
import com.exemple.backend.dto.PresenceDto;
import com.exemple.backend.dto.SessionDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints sessions/presences. Les shapes d'entree sont validees (B4) ;
 * les reponses 201 portent la ressource creee (EF1, EF2, EF11, EF12).
 */
@RestController
@RequestMapping("/api")
public class SessionController {

	private final SessionService sessionService;
	private final RelectureService relectureService;

	public SessionController(SessionService sessionService, RelectureService relectureService) {
		this.sessionService = sessionService;
		this.relectureService = relectureService;
	}

	public record SessionInput(@NotNull Long formateurId) {
	}

	public record PresenceInput(@NotBlank String code, @NotNull Long etudiantId) {
	}

	public record PresenceManuelleInput(@NotNull Long etudiantId) {
	}

	@PostMapping("/sessions")
	public ResponseEntity<SessionDto> ouvrir(@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid SessionInput input) {
		Session session = sessionService.ouvrir(input.formateurId());
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(session));
	}

	@PostMapping("/presences")
	public ResponseEntity<PresenceDto> marquerPresence(@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid PresenceInput input) {
		Presence presence = sessionService.marquerPresence(input.code(), input.etudiantId());
		// RG13 : une nouvelle presence peut liberer une assignation en attente.
		relectureService.tenterAssignations(presence.getSession().getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(presence));
	}

	@PostMapping("/sessions/{id}/presences")
	public ResponseEntity<PresenceDto> presenceManuelle(@PathVariable Long id,
			@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid PresenceManuelleInput input) {
		Presence presence = sessionService.ajouterPresenceManuelle(id, input.etudiantId());
		relectureService.tenterAssignations(id);
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(presence));
	}

	@PostMapping("/sessions/{id}/cloture")
	public ResponseEntity<SessionDto> cloturer(@PathVariable Long id) {
		return ResponseEntity.ok(toDto(sessionService.cloturer(id)));
	}

	private static SessionDto toDto(Session session) {
		return new SessionDto(session.getId(), session.getFormateurId(), session.getCode(),
				session.getOuverture(), session.getExpirationCode(), session.isCloturee());
	}

	private static PresenceDto toDto(Presence presence) {
		return new PresenceDto(presence.getId(), presence.getSession().getId(),
				presence.getEtudiantId(), presence.getHorodatage(), presence.getSource());
	}
}
