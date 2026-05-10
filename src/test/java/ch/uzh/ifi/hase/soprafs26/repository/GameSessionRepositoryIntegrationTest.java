package ch.uzh.ifi.hase.soprafs26.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;

@DataJpaTest
public class GameSessionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Test
    public void findByCode_existingSession_returnsSession() {
        GameSession session = new GameSession();
        session.setCode("ABC123");
        session.setCreatorId(1L);
        session.setCreatorUsername("host");
        session.setStatus(SessionStatus.WAITING);
        session.setCreatedAt(LocalDateTime.now());

        entityManager.persist(session);
        entityManager.flush();

        GameSession found = gameSessionRepository.findByCode("ABC123");

        assertNotNull(found);
        assertEquals("ABC123", found.getCode());
        assertEquals(1L, found.getCreatorId());
        assertEquals(SessionStatus.WAITING, found.getStatus());
    }

    @Test
    public void findByCode_nonExistentCode_returnsNull() {
        GameSession found = gameSessionRepository.findByCode("ZZZZZZ");

        assertNull(found);
    }

    @Test
    public void findByCode_sessionWithJoiner_returnsFullSession() {
        GameSession session = new GameSession();
        session.setCode("XYZ999");
        session.setCreatorId(1L);
        session.setCreatorUsername("host");
        session.setJoinerId(2L);
        session.setJoinerUsername("guest");
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        session.setStartedAt(LocalDateTime.now());

        entityManager.persist(session);
        entityManager.flush();

        GameSession found = gameSessionRepository.findByCode("XYZ999");

        assertNotNull(found);
        assertEquals(2L, found.getJoinerId());
        assertEquals("guest", found.getJoinerUsername());
        assertEquals(SessionStatus.ACTIVE, found.getStatus());
    }
}
