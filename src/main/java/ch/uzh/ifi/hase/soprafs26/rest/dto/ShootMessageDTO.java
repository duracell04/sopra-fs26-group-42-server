package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class ShootMessageDTO {

    private Long playerId;
    private double x;
    private double y;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
