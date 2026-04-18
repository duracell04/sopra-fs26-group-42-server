package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.GameBlock;
import ch.uzh.ifi.hase.soprafs26.constant.GameBlockState;

class GameServiceTest {

    private GameService gameService = new GameService();

    @Test
    void testIncorrectPairDeductsOneLife() {
 
        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(3);
        game.setGameOver(false);

        GameBlock block1 = new GameBlock();
        block1.setValue(2);
        GameBlock block2 = new GameBlock();
        block2.setValue(3); // 2*3=6, not 12

        gameService.resolvePair(game, block1, block2);

        assertEquals(2, game.getSharedLives());
        assertFalse(game.isGameOver());
        assertEquals(GameBlockState.INCORRECT, block1.getState());
        assertEquals(GameBlockState.INCORRECT, block2.getState());
    }

    @Test
    void testIncorrectPairAtLastLifeTriggersGameOver() {

        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(1);
        game.setGameOver(false);

        GameBlock block1 = new GameBlock();
        block1.setValue(2);
        GameBlock block2 = new GameBlock();
        block2.setValue(3); // incorrect

        gameService.resolvePair(game, block1, block2);


        assertEquals(0, game.getSharedLives());
        assertTrue(game.isGameOver());
    }

    @Test
    void testCorrectPairDoesNotDeductLife() {

        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(3);
        game.setScore(0);

        GameBlock block1 = new GameBlock();
        block1.setValue(3);
        GameBlock block2 = new GameBlock();
        block2.setValue(4); // 3*4=12, correct

        gameService.resolvePair(game, block1, block2);

        assertEquals(3, game.getSharedLives()); // unchanged
        assertFalse(game.isGameOver());
        assertEquals(GameBlockState.ELIMINATED, block1.getState());
        assertEquals(GameBlockState.ELIMINATED, block2.getState());
        assertEquals(1, game.getScore());
    }

    @Test
    void testGameOverBlocksFurtherGameplay() {

        Game game = new Game();
        game.setTargetProduct(12);
        game.setSharedLives(0);
        game.setGameOver(true);

        GameBlock block1 = new GameBlock();
        block1.setValue(3);
        GameBlock block2 = new GameBlock();
        block2.setValue(4); // would be correct, but game is over
        
        gameService.resolvePair(game, block1, block2);

        assertEquals(0, game.getSharedLives());
        assertTrue(game.isGameOver());
        assertEquals(GameBlockState.DEFAULT, block1.getState()); // unchanged
        assertEquals(GameBlockState.DEFAULT, block2.getState()); // unchanged
    }
}