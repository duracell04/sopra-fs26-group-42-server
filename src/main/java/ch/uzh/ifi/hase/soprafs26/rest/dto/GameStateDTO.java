package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class GameStateDTO {

    private String sessionCode;
    private Long gameId;
    private int score;
    private int sharedLives;
    private boolean gameOver;
    private boolean errorTriggered;
    private Long pendingSelectionBlockId;
    private List<GameBlockStateDTO> blocks;
    private int targetProduct;

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getSharedLives() {
        return sharedLives;
    }

    public void setSharedLives(int sharedLives) {
        this.sharedLives = sharedLives;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isErrorTriggered() {
        return errorTriggered;
    }

    public void setErrorTriggered(boolean errorTriggered) {
        this.errorTriggered = errorTriggered;
    }

    public Long getPendingSelectionBlockId() {
        return pendingSelectionBlockId;
    }

    public void setPendingSelectionBlockId(Long pendingSelectionBlockId) {
        this.pendingSelectionBlockId = pendingSelectionBlockId;
    }

    public List<GameBlockStateDTO> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<GameBlockStateDTO> blocks) {
        this.blocks = blocks;
    }

    public int getTargetProduct() {
        return targetProduct;
    }

    public void setTargetProduct(int targetProduct) {
        this.targetProduct = targetProduct;
    }
}