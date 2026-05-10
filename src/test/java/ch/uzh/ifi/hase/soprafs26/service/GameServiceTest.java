package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.GameBlock;
import ch.uzh.ifi.hase.soprafs26.constant.GameBlockState;

public class GameServiceTest {

    private final GameService gameService = new GameService();

    @Test
    void correctPair_incrementsScore_eliminatesBlocks_clearsPending() {
        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(3);
        game.setScore(0);
        game.setPendingSelection(42L);

        GameBlock block1 = new GameBlock();
        block1.setValue(3);
        GameBlock block2 = new GameBlock();
        block2.setValue(4); // 3*4=12

        gameService.resolvePair(game, block1, block2);

        assertEquals(1, game.getScore());
        assertEquals(3, game.getSharedLives());
        assertFalse(game.isGameOver());
        assertNull(game.getPendingSelection());
        assertEquals(GameBlockState.ELIMINATED, block1.getState());
        assertEquals(GameBlockState.ELIMINATED, block2.getState());
    }

    @Test
    void incorrectPair_deductsLife_marksIncorrect_clearsPending() {
        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(3);
        game.setScore(0);
        game.setPendingSelection(7L);

        GameBlock block1 = new GameBlock();
        block1.setValue(2);
        GameBlock block2 = new GameBlock();
        block2.setValue(3); // 2*3=6, not 12

        gameService.resolvePair(game, block1, block2);

        assertEquals(2, game.getSharedLives());
        assertEquals(0, game.getScore());
        assertFalse(game.isGameOver());
        assertNull(game.getPendingSelection());
        assertEquals(GameBlockState.INCORRECT, block1.getState());
        assertEquals(GameBlockState.INCORRECT, block2.getState());
    }

    @Test
    void incorrectPairAtLastLife_triggersGameOver() {
        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(1);

        GameBlock block1 = new GameBlock();
        block1.setValue(2);
        GameBlock block2 = new GameBlock();
        block2.setValue(3); // incorrect

        gameService.resolvePair(game, block1, block2);

        assertEquals(0, game.getSharedLives());
        assertTrue(game.isGameOver());
    }

    @Test
    void gameAlreadyOver_resolvePairIsNoOp() {
        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(0);
        game.setGameOver(true);
        game.setScore(5);

        GameBlock block1 = new GameBlock();
        block1.setValue(3);
        GameBlock block2 = new GameBlock();
        block2.setValue(4); // would be correct, but game is over

        gameService.resolvePair(game, block1, block2);

        assertEquals(0, game.getSharedLives());
        assertTrue(game.isGameOver());
        assertEquals(5, game.getScore());
        assertEquals(GameBlockState.DEFAULT, block1.getState());
        assertEquals(GameBlockState.DEFAULT, block2.getState());
    }

    @Test
    void multipleCorrectPairs_scoreAccumulates() {
        Game game = new Game();
        game.setTargetProduct(6);
        game.setSharedLives(3);
        game.setScore(0);

        GameBlock a1 = new GameBlock(); a1.setValue(2);
        GameBlock a2 = new GameBlock(); a2.setValue(3); // 2*3=6
        GameBlock b1 = new GameBlock(); b1.setValue(1);
        GameBlock b2 = new GameBlock(); b2.setValue(6); // 1*6=6

        gameService.resolvePair(game, a1, a2);
        gameService.resolvePair(game, b1, b2);

        assertEquals(2, game.getScore());
        assertEquals(3, game.getSharedLives());
    }

    @Test
    void correctPair_livesRemainUnchanged() {
        Game game = new Game();
        game.setTargetProduct(20);
        game.setSharedLives(2);

        GameBlock block1 = new GameBlock(); block1.setValue(4);
        GameBlock block2 = new GameBlock(); block2.setValue(5); // 4*5=20

        gameService.resolvePair(game, block1, block2);

        assertEquals(2, game.getSharedLives());
        assertFalse(game.isGameOver());
    }
}
