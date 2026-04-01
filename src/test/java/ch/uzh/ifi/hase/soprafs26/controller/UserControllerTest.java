package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);

		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is(user.getName())))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void logoutUser_validInput_returns204() throws Exception {
		// given
		doNothing().when(userService).logoutUser(1L);

		// when
		MockHttpServletRequestBuilder putRequest = put("/users/1/logout")
				.contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(putRequest).andExpect(status().isNoContent());
	}

	@Test
	public void loginUser_validInput_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setPassword("password123");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserLoginDTO userLoginDTO = new UserLoginDTO();
		userLoginDTO.setUsername("testUsername");
		userLoginDTO.setPassword("password123");

		given(userService.loginUser("testUsername", "password123")).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userLoginDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.token", is(user.getToken())));
	}

	@Test
	public void getUserProfile_validInput_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setStatus(UserStatus.ONLINE);
		user.setHighestScore(25);
		user.setTotalScore(100);
		user.setTimePlayed(360L);
		user.setCreationDate(java.time.LocalDate.of(2026, 3, 29));

		given(userService.getUserProfile(1L)).willReturn(user);

		MockHttpServletRequestBuilder getRequest = get("/users/1/profile")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId", is(1)))
				.andExpect(jsonPath("$.username", is("testUsername")))
				.andExpect(jsonPath("$.joinDate", is("2026-03-29")))
				.andExpect(jsonPath("$.highestScore", is(25)))
				.andExpect(jsonPath("$.totalScore", is(100)))
				.andExpect(jsonPath("$.timePlayed", is(360)));
	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}
