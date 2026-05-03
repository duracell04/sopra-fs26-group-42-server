package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class GameSummaryGetDTO {

    private Integer score;
    private Long elapsedSeconds;
    private Boolean newHighscore;
    private String feedback;
    private Integer totalScore;
    private Integer highestScore;
    private Long timePlayed;

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Long getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(Long elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }

    public Boolean getNewHighscore() { return newHighscore; }
    public void setNewHighscore(Boolean newHighscore) { this.newHighscore = newHighscore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

    public Long getTimePlayed() { return timePlayed; }
    public void setTimePlayed(Long timePlayed) { this.timePlayed = timePlayed; }
}
