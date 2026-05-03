package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ProblemsDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionJoinPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameSummaryService;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;

import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class GameSessionController {

    private final GameSessionService gameSessionService;
    private final GameSummaryService gameSummaryService;

    public GameSessionController(GameSessionService gameSessionService, GameSummaryService gameSummaryService) {
        this.gameSessionService = gameSessionService;
        this.gameSummaryService = gameSummaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SessionGetDTO createSession(@RequestBody SessionPostDTO sessionPostDTO) {
        return gameSessionService.createSession(sessionPostDTO.getCreatorId());
    }

    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SessionGetDTO getSession(@PathVariable String code) {
        return gameSessionService.getSession(code);
    }

    @PostMapping("/{code}/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SessionGetDTO joinSession(@PathVariable String code, @RequestBody SessionJoinPostDTO sessionJoinPostDTO) {
        return gameSessionService.joinSession(code, sessionJoinPostDTO.getUserId());
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSession(@PathVariable String code, @RequestParam Long userId) {
        gameSessionService.cancelSession(code, userId);
    }

    @PostMapping("/{code}/problems")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveProblems(@PathVariable String code, @RequestBody ProblemsDTO dto) {
        gameSessionService.saveProblems(code, dto.getProblemsJson());
    }

    @GetMapping("/{code}/problems")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ProblemsDTO getProblems(@PathVariable String code) {
        ProblemsDTO dto = new ProblemsDTO();
        dto.setProblemsJson(gameSessionService.getProblems(code));
        return dto;
    }

    @PostMapping("/{code}/start")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SessionGetDTO startGame(@PathVariable String code, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        return gameSessionService.startGame(code, userId);
    }

    @PostMapping("/{code}/finish")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SessionGetDTO finishGame(@PathVariable String code, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        return gameSessionService.finishGame(code, userId);
    }

    @PostMapping("/{code}/summary")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameSummaryGetDTO createSummary(@PathVariable String code, @RequestBody GameSummaryPostDTO summaryPostDTO) {
        return gameSummaryService.createSummary(code, summaryPostDTO);
    }
}
