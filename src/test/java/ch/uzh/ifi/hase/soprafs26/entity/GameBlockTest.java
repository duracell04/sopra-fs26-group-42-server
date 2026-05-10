package ch.uzh.ifi.hase.soprafs26.entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import ch.uzh.ifi.hase.soprafs26.constant.GameBlockState;

public class GameBlockTest {

    @Test
    void newBlock_hasDefaultState() {
        GameBlock block = new GameBlock();
        assertEquals(GameBlockState.DEFAULT, block.getState());
    }

    @Test
    void setValue_storesValue() {
        GameBlock block = new GameBlock();
        block.setValue(7);
        assertEquals(7, block.getValue());
    }

    @Test
    void setState_updatesState() {
        GameBlock block = new GameBlock();
        block.setState(GameBlockState.SELECTED);
        assertEquals(GameBlockState.SELECTED, block.getState());
    }

    @Test
    void setSelectedByUserId_storesUserId() {
        GameBlock block = new GameBlock();
        block.setSelectedByUserId(42L);
        assertEquals(42L, block.getSelectedByUserId());
    }

    @Test
    void setSelectedUntil_storesInstant() {
        GameBlock block = new GameBlock();
        Instant now = Instant.now();
        block.setSelectedUntil(now);
        assertEquals(now, block.getSelectedUntil());
    }

    @Test
    void setId_storesId() {
        GameBlock block = new GameBlock();
        block.setId(99L);
        assertEquals(99L, block.getId());
    }
}
