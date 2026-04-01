package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserProfileGetDTO {

	private Long userId;
	private String username;
	private String joinDate;
	private Integer highestScore;
	private Integer totalScore;
	private Long timePlayed;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
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