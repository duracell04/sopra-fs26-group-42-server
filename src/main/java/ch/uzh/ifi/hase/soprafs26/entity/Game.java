package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int targetProduct;
    private int score;
    private Long pendingSelection;

    private int sharedLives = 3;
    private boolean gameOver = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTargetProduct() {
        return targetProduct;
    }

    public void setTargetProduct(int targetProduct) {
        this.targetProduct = targetProduct;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Long getPendingSelection() {
        return pendingSelection;
    }

    public void setPendingSelection(Long pendingSelection) {
        this.pendingSelection = pendingSelection;
    }

    public int getSharedLives() {
        return sharedLives;
    }

    public void setSharedLives(int sharedLives) {
        this.sharedLives = Math.max(0, sharedLives);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }


}
