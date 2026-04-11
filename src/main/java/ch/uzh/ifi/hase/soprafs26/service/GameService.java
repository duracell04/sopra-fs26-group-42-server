package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.GameBlock;
import ch.uzh.ifi.hase.soprafs26.constant.GameBlockState;

@Service
@Transactional
public class GameService {

    public Game resolvePair(Game game, GameBlock firstBlock, GameBlock currentBlock) {
        if (isMatchingPair(firstBlock, currentBlock, game.getTargetProduct())) {
            firstBlock.setState(GameBlockState.ELIMINATED);
            currentBlock.setState(GameBlockState.ELIMINATED);
            game.setScore(game.getScore() + 1);
            game.setPendingSelection(null);
        } else {
            firstBlock.setState(GameBlockState.INCORRECT);
            currentBlock.setState(GameBlockState.INCORRECT);
            game.setPendingSelection(null);
        }

        return game;
    }

    private boolean isMatchingPair(GameBlock block1, GameBlock block2, int targetProduct) {
        if (block1 == null || block2 == null) {
            return false;
        }
        return block1.getValue() * block2.getValue() == targetProduct;
    }
}