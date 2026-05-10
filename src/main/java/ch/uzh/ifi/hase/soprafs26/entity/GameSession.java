package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String code;

    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String creatorUsername;

    @Column
    private Long joinerId;

    @Column
    private String joinerUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column(columnDefinition = "TEXT")
    private String problemsJson;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean creatorSummarySubmitted = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean joinerSummarySubmitted = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public Long getJoinerId() { return joinerId; }
    public void setJoinerId(Long joinerId) { this.joinerId = joinerId; }

    public String getJoinerUsername() { return joinerUsername; }
    public void setJoinerUsername(String joinerUsername) { this.joinerUsername = joinerUsername; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public String getProblemsJson() { return problemsJson; }
    public void setProblemsJson(String problemsJson) { this.problemsJson = problemsJson; }

    public boolean isCreatorSummarySubmitted() { return creatorSummarySubmitted; }
    public void setCreatorSummarySubmitted(boolean creatorSummarySubmitted) {
        this.creatorSummarySubmitted = creatorSummarySubmitted;
    }

    public boolean isJoinerSummarySubmitted() { return joinerSummarySubmitted; }
    public void setJoinerSummarySubmitted(boolean joinerSummarySubmitted) {
        this.joinerSummarySubmitted = joinerSummarySubmitted;
    }
}
