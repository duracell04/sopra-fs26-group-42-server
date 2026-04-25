package ch.uzh.ifi.hase.soprafs26.repository;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class UserRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void findByUsername_success() {
		// given
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setPassword("password123");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");
		user.setCreationDate(LocalDate.now());

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByUsername(user.getUsername());

		// then
		assertNotNull(found.getId());
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getToken(), user.getToken());
		assertEquals(found.getStatus(), user.getStatus());
	}

	@Test
	public void findByUsername_unknownUsername_returnsNull() {
		// when
		User found = userRepository.findByUsername("unknownUser");

		// then
		assertEquals(null, found);
	}

	@Test
	public void findByToken_success() {
		// given
		User user = new User();
		user.setUsername("tokenUser");
		user.setPassword("password123");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("valid-token");
		user.setCreationDate(LocalDate.now());

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByToken(user.getToken());

		// then
		assertNotNull(found);
		assertEquals(user.getUsername(), found.getUsername());
		assertEquals(user.getToken(), found.getToken());
		assertEquals(user.getStatus(), found.getStatus());
	}

	@Test
	public void saveUser_withProfileStats_success() {
		// given
		User user = new User();
		user.setUsername("profileUser");
		user.setPassword("password123");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("profile-token");
		user.setCreationDate(LocalDate.of(2026, 3, 29));
		user.setHighestScore(25);
		user.setTotalScore(100);
		user.setTimePlayed(360L);

		// when
		User savedUser = userRepository.save(user);
		entityManager.flush();

		// then
		assertNotNull(savedUser.getId());
		assertEquals("profileUser", savedUser.getUsername());
		assertEquals(LocalDate.of(2026, 3, 29), savedUser.getCreationDate());
		assertEquals(25, savedUser.getHighestScore());
		assertEquals(100, savedUser.getTotalScore());
		assertEquals(360L, savedUser.getTimePlayed());
	}
}
