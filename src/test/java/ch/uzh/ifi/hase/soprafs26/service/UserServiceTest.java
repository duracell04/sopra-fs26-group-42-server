package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testUsername");
		testUser.setPassword("password123");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void getUsers_returnsRepositoryUsers() {
		Mockito.when(userRepository.findAll()).thenReturn(List.of(testUser));

		List<User> users = userService.getUsers();

		assertEquals(1, users.size());
		assertEquals(testUser.getUsername(), users.get(0).getUsername());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void logoutUser_validInput_statusSetToOffline() {
		// given
		testUser.setStatus(UserStatus.ONLINE);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		// when
		userService.logoutUser(1L);

		// then
		assertEquals(UserStatus.OFFLINE, testUser.getStatus());
	}

	@Test
	public void logoutUser_userNotFound_throwsException() {
		// given
		Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

		// then
		assertThrows(ResponseStatusException.class, () -> userService.logoutUser(99L));
	}

	@Test
	public void loginUser_validCredentials_setsStatusOnline() {
		testUser.setPassword("password123");
		testUser.setStatus(UserStatus.OFFLINE);
		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);

		User loggedInUser = userService.loginUser("testUsername", "password123");

		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertEquals(testUser.getUsername(), loggedInUser.getUsername());
		Mockito.verify(userRepository, Mockito.times(1)).flush();
	}

	@Test
	public void loginUser_unknownUsername_throwsException() {
		Mockito.when(userRepository.findByUsername("missingUser")).thenReturn(null);

		assertThrows(ResponseStatusException.class, () -> userService.loginUser("missingUser", "password123"));
	}

	@Test
	public void loginUser_wrongPassword_throwsException() {
		testUser.setPassword("correctPassword");
		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);

		assertThrows(ResponseStatusException.class, () -> userService.loginUser("testUsername", "wrongPassword"));
	}

	@Test
	public void getUserProfile_validId_success() {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		User foundUser = userService.getUserProfile(1L);

		assertEquals(testUser.getId(), foundUser.getId());
		assertEquals(testUser.getUsername(), foundUser.getUsername());
	}

	@Test
	public void getUserProfile_unknownId_throwsException() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.getUserProfile(99L));
	}

}
