package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ShootMessageDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.MoveMessageDTO;

@Controller
public class GameWebSocketController {

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
}
