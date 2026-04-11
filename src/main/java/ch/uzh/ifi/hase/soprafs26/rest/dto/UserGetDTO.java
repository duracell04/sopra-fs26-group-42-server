package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import java.time.LocalDate;

public class UserGetDTO {

	private Long id;
	private String username;
	private String token;
	private UserStatus status;
	private LocalDate creationDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}
}
