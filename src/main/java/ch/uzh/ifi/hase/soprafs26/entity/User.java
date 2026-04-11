package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private UserStatus status;

	@Column(nullable = false)
	private LocalDate creationDate;

	@Column(nullable = false)
	private Integer highestScore = 0;

	@Column(nullable = false)
	private Integer totalScore = 0;

	@Column(nullable = false)
	private Long timePlayed = 0L;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Integer getHighestScore() {
		return highestScore;
	}

	public void setHighestScore(Integer highestScore) {
		this.highestScore = highestScore;
	}

	public Integer getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Integer totalScore) {
		this.totalScore = totalScore;
	}

	public Long getTimePlayed() {
		return timePlayed;
	}

	public void setTimePlayed(Long timePlayed) {
		this.timePlayed = timePlayed;
	}
}
