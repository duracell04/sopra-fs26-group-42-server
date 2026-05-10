package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserProfileGetDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
	@Test
	public void testCreateUser_fromUserPostDTO_toUser_success() {
		// create UserPostDTO
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("username");
		userPostDTO.setPassword("password123");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getUsername(), user.getUsername());
		assertEquals(userPostDTO.getPassword(), user.getPassword());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");

		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
	}

	@Test
	public void testGetUserProfile_fromUser_toUserProfileGetDTO_mapsAllFields() {
		User user = new User();
		user.setId(5L);
		user.setUsername("profileUser");
		user.setCreationDate(LocalDate.of(2026, 3, 29));
		user.setHighestScore(42);
		user.setTotalScore(200);
		user.setTimePlayed(3600L);

		UserProfileGetDTO dto = DTOMapper.INSTANCE.convertEntityToUserProfileGetDTO(user);

		assertEquals(5L, dto.getUserId());
		assertEquals("profileUser", dto.getUsername());
		assertEquals("2026-03-29", dto.getJoinDate());
		assertEquals(42, dto.getHighestScore());
		assertEquals(200, dto.getTotalScore());
		assertEquals(3600L, dto.getTimePlayed());
	}

	@Test
	public void testConvertUserPostDTO_ignoredFields_areNull() {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("newUser");
		userPostDTO.setPassword("secret");

		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		assertNull(user.getId());
		assertNull(user.getToken());
		assertNull(user.getStatus());
		assertNull(user.getCreationDate());
	}
}
