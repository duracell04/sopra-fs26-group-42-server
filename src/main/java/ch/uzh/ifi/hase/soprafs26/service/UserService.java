package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setCreationDate(LocalDate.now());
		newUser.setHighestScore(0);
		newUser.setTotalScore(0);
		newUser.setTimePlayed(0L);

		checkIfUserExists(newUser);
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	public User loginUser(String username, String password) {
		User user = userRepository.findByUsername(username);

		if (user == null || !user.getPassword().equals(password)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
		}

		user.setStatus(UserStatus.ONLINE);
		userRepository.flush();

		return user;
	}

	public void logoutUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"User with id " + id + " was not found"));
		user.setStatus(UserStatus.OFFLINE);
		userRepository.flush();
	}

	public User getUserProfile(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"User with id " + userId + " was not found"));
	}

	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

		if (userByUsername != null) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"The username provided is not unique. Therefore, the user could not be created!"
			);
		}
	}
}
