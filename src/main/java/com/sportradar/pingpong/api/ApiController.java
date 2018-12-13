package com.sportradar.pingpong.api;

import com.sportradar.pingpong.api.domain.Player;
import com.sportradar.pingpong.api.dto.ResultDto;
import com.sportradar.pingpong.api.services.PingPongStorer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequiredArgsConstructor
public class ApiController {
  private final PingPongStorer pingPongStorer;

  @CrossOrigin
  @RequestMapping(
      value = "/result",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResultDto getResult() {
    return pingPongStorer.getResult();
  }

  @CrossOrigin
  @RequestMapping(value = "/addPoint/{player}", method = RequestMethod.GET)
  public Integer addPoint(@PathVariable("player") Player player) {
    return pingPongStorer.addPoint(player);
  }

  @CrossOrigin
  @RequestMapping(value = "/undoPoint", method = RequestMethod.GET)
  public void undoPoint() {
    pingPongStorer.removePoint();
  }

  @CrossOrigin
  @RequestMapping(value = "/matchStart", method = RequestMethod.GET)
  public Integer matchStart(
      @RequestParam(name = "player1", defaultValue = "1") Integer player1,
      @RequestParam(name = "player2", defaultValue = "2") Integer player2,
      @RequestParam(name = "bestOf", defaultValue = "3") Integer bestOf,
      @RequestParam(name = "totalPointsInSet", defaultValue = "11") Integer totalPointsInSet) {
    return pingPongStorer.startGame(player1, player2, bestOf, totalPointsInSet);
  }

  @CrossOrigin
  @RequestMapping(value = "/chooseGame", method = RequestMethod.GET)
  public String chooseGame() {

    return "ok";
  }
}
