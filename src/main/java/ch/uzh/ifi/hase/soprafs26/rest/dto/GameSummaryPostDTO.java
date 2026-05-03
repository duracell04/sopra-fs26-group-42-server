package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class GameSummaryPostDTO {

    private Long userId;
    private Integer score;
    private Long elapsedSeconds;
    private Integer livesRemaining;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Long getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(Long elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }

    public Integer getLivesRemaining() { return livesRemaining; }
    public void setLivesRemaining(Integer livesRemaining) { this.livesRemaining = livesRemaining; }
}
