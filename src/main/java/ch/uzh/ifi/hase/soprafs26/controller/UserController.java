package ch.uzh.ifi.hase.soprafs26.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserProfileGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController

public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}


	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers() {
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@PutMapping("/users/{id}/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logoutUser(@PathVariable Long id) {
		userService.logoutUser(id);
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@Valid @RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

	@PostMapping("/users/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO loginUser(@RequestBody UserLoginDTO userLoginDTO) {
		User loggedInUser = userService.loginUser(userLoginDTO.getUsername(), userLoginDTO.getPassword());
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
	}

	@GetMapping("/users/{userId}/profile")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserProfileGetDTO getUserProfile(@PathVariable Long userId) {
		User user = userService.getUserProfile(userId);
		return DTOMapper.INSTANCE.convertEntityToUserProfileGetDTO(user);
	}
}
