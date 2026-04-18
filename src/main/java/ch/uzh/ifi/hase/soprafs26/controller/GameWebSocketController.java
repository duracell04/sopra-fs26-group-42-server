package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import ch.uzh.ifi.hase.soprafs26.constant.GameBlockState;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.entity.GameBlock;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BlockSelectionMessageDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameBlockStateDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShootMessageDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.MoveMessageDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class GameWebSocketController {

    private final GameService gameService;
    private final Map<String, Game> gamesBySession = new ConcurrentHashMap<>();
    private final Map<String, PendingSelection> pendingSelectionBySession = new ConcurrentHashMap<>();

    private static class PendingSelection {
        private final Long blockId;
        private final int blockValue;

        private PendingSelection(Long blockId, int blockValue) {
            this.blockId = blockId;
            this.blockValue = blockValue;
        }
    }

    public GameWebSocketController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/shoot")
    @SendTo("/topic/game")
    public ShootMessageDTO handleShoot(ShootMessageDTO message) {
        return message;
    }

    @MessageMapping("/move")
    @SendTo("/topic/game")
    public MoveMessageDTO handleMove(MoveMessageDTO message) {
        return message;
    }

    // Resolves block pairs per session and emits a game state update.
    @MessageMapping("/selectBlock")
    @SendTo("/topic/game")
    public GameStateDTO handleBlockSelection(BlockSelectionMessageDTO selection) {
        if (selection == null || !StringUtils.hasText(selection.getSessionCode())) {
            return new GameStateDTO();
        }

        final String sessionCode = selection.getSessionCode();
        Game game = gamesBySession.computeIfAbsent(sessionCode, key -> {
            Game newGame = new Game();
            newGame.setSharedLives(3);
            newGame.setGameOver(false);
            newGame.setScore(0);
            return newGame;
        });

        if (selection.getTargetProduct() > 0) {
            game.setTargetProduct(selection.getTargetProduct());
        }

        if (game.isGameOver()) {
            return toStateDTO(sessionCode, game, false, List.of());
        }

        PendingSelection pending = pendingSelectionBySession.get(sessionCode);
        if (pending == null || sameBlock(pending.blockId, selection.getBlockId())) {
            pendingSelectionBySession.put(sessionCode,
                    new PendingSelection(selection.getBlockId(), selection.getBlockValue()));
            game.setPendingSelection(selection.getBlockId());
            return toStateDTO(sessionCode, game, false, List.of(toBlockState(selection.getBlockId(), selection.getBlockValue(), GameBlockState.SELECTED)));
        }

        GameBlock firstBlock = new GameBlock();
        firstBlock.setId(pending.blockId);
        firstBlock.setValue(pending.blockValue);

        GameBlock currentBlock = new GameBlock();
        currentBlock.setId(selection.getBlockId());
        currentBlock.setValue(selection.getBlockValue());

        gameService.resolvePair(game, firstBlock, currentBlock);

        pendingSelectionBySession.remove(sessionCode);
        boolean errorTriggered = firstBlock.getState() == GameBlockState.INCORRECT
                || currentBlock.getState() == GameBlockState.INCORRECT;

        List<GameBlockStateDTO> changedBlocks = new ArrayList<>();
        changedBlocks.add(toBlockState(firstBlock.getId(), firstBlock.getValue(), firstBlock.getState()));
        changedBlocks.add(toBlockState(currentBlock.getId(), currentBlock.getValue(), currentBlock.getState()));

        return toStateDTO(sessionCode, game, errorTriggered, changedBlocks);
    }

    private boolean sameBlock(Long block1, Long block2) {
        return block1 != null && block1.equals(block2);
    }

    private GameStateDTO toStateDTO(String sessionCode, Game game, boolean errorTriggered, List<GameBlockStateDTO> blocks) {
        GameStateDTO state = new GameStateDTO();
        state.setSessionCode(sessionCode);
        state.setGameId(game.getId());
        state.setScore(game.getScore());
        state.setSharedLives(game.getSharedLives());
        state.setGameOver(game.isGameOver());
        state.setErrorTriggered(errorTriggered);
        state.setPendingSelectionBlockId(game.getPendingSelection());
        state.setTargetProduct(game.getTargetProduct());
        state.setBlocks(blocks);
        return state;
    }

    private GameBlockStateDTO toBlockState(Long blockId, int value, GameBlockState state) {
        GameBlockStateDTO dto = new GameBlockStateDTO();
        dto.setId(blockId);
        dto.setValue(value);
        dto.setState(state.name());
        return dto;
    }

}
