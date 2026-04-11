package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.time.Instant;
import ch.uzh.ifi.hase.soprafs26.constant.GameBlockState;

@Entity
public class GameBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int value;

    @Enumerated(EnumType.STRING)
    private GameBlockState state = GameBlockState.DEFAULT;
    private Long selectedByUserId;
    private Instant selectedUntil;

    // Getter & Setter 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public GameBlockState getState() {
        return state;
    }

    public void setState(GameBlockState state) {
        this.state = state;
    }

    public Long getSelectedByUserId() {
        return selectedByUserId;
    }

    public void setSelectedByUserId(Long selectedByUserId) {
        this.selectedByUserId = selectedByUserId;
    }

    public Instant getSelectedUntil() {
        return selectedUntil;
    }

    public void setSelectedUntil(Instant selectedUntil) {
        this.selectedUntil = selectedUntil;
    }

}