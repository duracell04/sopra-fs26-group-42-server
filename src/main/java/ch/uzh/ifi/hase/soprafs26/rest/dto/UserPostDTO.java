package ch.uzh.ifi.hase.soprafs26.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserPostDTO {

	@NotBlank(message = "Username must not be empty")
	@Size(min = 3, max = 20, message = "Username must be 3-20 characters")
	private String username;

	@NotBlank(message = "Password must not be empty")
	@Size(min = 6, max = 15, message = "Password must be 6-15 characters")
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
