package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class GameSessionService {

    private final GameSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GameSessionService(GameSessionRepository sessionRepository,
                              UserRepository userRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public SessionGetDTO createSession(Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        GameSession session = new GameSession();
        session.setCode(generateUniqueCode());
        session.setCreatorId(creatorId);
        session.setCreatorUsername(creator.getUsername());
        session.setStatus(SessionStatus.WAITING);
        session.setCreatedAt(LocalDateTime.now());

        session = sessionRepository.save(session);
        sessionRepository.flush();

        SessionGetDTO dto = convertToDTO(session);
        messagingTemplate.convertAndSend("/topic/session/" + session.getCode(), dto);
        return dto;
    }

    public SessionGetDTO getSession(String code) {
        GameSession session = findAndCheckExpiry(code);
        return convertToDTO(session);
    }

    public void cancelSession(String code, Long userId) {
        GameSession session = findSession(code);
        if (!session.getCreatorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the session creator can cancel the session");
        }
        session.setStatus(SessionStatus.CANCELLED);
        sessionRepository.flush();

        messagingTemplate.convertAndSend("/topic/session/" + code, convertToDTO(session));
    }

    public SessionGetDTO startGame(String code, Long userId) {
        GameSession session = findAndCheckExpiry(code);
        if (!session.getCreatorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the session creator can start the game");
        }
        if (session.getJoinerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need two players to start the game");
        }
        session.setStatus(SessionStatus.ACTIVE);
        sessionRepository.flush();

        SessionGetDTO dto = convertToDTO(session);
        messagingTemplate.convertAndSend("/topic/session/" + code, dto);
        return dto;
    }

    private GameSession findSession(String code) {
        GameSession session = sessionRepository.findByCode(code);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session with code '" + code + "' not found");
        }
        return session;
    }

    private GameSession findAndCheckExpiry(String code) {
        GameSession session = findSession(code);
        // Expire session if waiting for more than 5 minutes with only 1 player
        if (session.getStatus() == SessionStatus.WAITING
                && session.getJoinerId() == null
                && session.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.CANCELLED);
            sessionRepository.flush();
            messagingTemplate.convertAndSend("/topic/session/" + code, convertToDTO(session));
        }
        return session;
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (sessionRepository.findByCode(code) != null);
        return code;
    }

    private SessionGetDTO convertToDTO(GameSession session) {
        SessionGetDTO dto = new SessionGetDTO();
        dto.setId(session.getId());
        dto.setCode(session.getCode());
        dto.setCreatorId(session.getCreatorId());
        dto.setStatus(session.getStatus().name());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setExpiresAt(session.getCreatedAt().plusMinutes(5));

        List<String> players = new ArrayList<>();
        players.add(session.getCreatorUsername());
        if (session.getJoinerUsername() != null) {
            players.add(session.getJoinerUsername());
        }
        dto.setPlayers(players);

        return dto;
    }
}
