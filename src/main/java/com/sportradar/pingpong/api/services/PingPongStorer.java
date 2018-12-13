package com.sportradar.pingpong.api.services;

import com.sportradar.pingpong.api.domain.Event;
import com.sportradar.pingpong.api.domain.Game;
import com.sportradar.pingpong.api.domain.Player;
import com.sportradar.pingpong.api.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class PingPongStorer {
  private final DataSource mysqlDataSource;
  private final JdbcTemplate jdbcTemplate;

  public Integer startGame(
      Integer player1, Integer player2, Integer bestOf, Integer totalPointsInSet) {
    return addNewGameInDB(player1, player2, bestOf, totalPointsInSet);
  }

  public Integer addNewGameInDB(
      Integer player1, Integer player2, Integer bestOf, Integer totalPointsInSet) {
    SimpleJdbcInsert gameJdbcInsert = new SimpleJdbcInsert(mysqlDataSource).withTableName("game");

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("player1", player1);
    parameters.put("player2", player2);
    parameters.put("bestOf", bestOf);
    parameters.put("pointsInSet", totalPointsInSet);
    parameters.put("date", new Date());

    return gameJdbcInsert.execute(parameters);
  }

  public Integer addPoint(Player player) {
    if (player == Player.PLAYER_ONE) {
      return addEvent(Event.POINT_PLAYER_ONE);
    }
    return addEvent(Event.POINT_PLAYER_TWO);
  }

  public Integer removePoint(Player player) {
    if (player == Player.PLAYER_ONE) {
      return addEvent(Event.UNDO_POINT_PLAYER_ONE);
    }
    return addEvent(Event.UNDO_POINT_PLAYER_TWO);
  }

  public ResultDto getResult() {
    Integer currentGameId = getLatestGameId();
    List<Event> gameEvents = getGameEvents(currentGameId);
    Game game = getGame(currentGameId);
    Integer bestOf = game.getBestOf();
    Integer totalPointsInSet = game.getPointsInSet();
    Integer playerOneSets = 0;
    Integer playerTwoSets = 0;
    Integer playerOnePoints = 0;
    Integer playerTwoPoints = 0;
    Player winner = Player.NONE;

    for (Event gameEvent : gameEvents) {
      if (gameEvent == Event.POINT_PLAYER_ONE) {
        playerOnePoints++;
      }
      if (gameEvent == Event.POINT_PLAYER_TWO) {
        playerTwoPoints++;
      }
      if (gameEvent == Event.UNDO_POINT_PLAYER_ONE) {
        playerOnePoints--;
      }
      if (gameEvent == Event.UNDO_POINT_PLAYER_TWO) {
        playerTwoPoints--;
      }

      Player playerWhoWonTheSet = whoWonTheSet(playerOnePoints, playerTwoPoints, totalPointsInSet);

      if (playerWhoWonTheSet == Player.PLAYER_ONE) {
        playerOnePoints = 0;
        playerTwoPoints = 0;
        playerOneSets++;
      }
      if (playerWhoWonTheSet == Player.PLAYER_TWO) {
        playerOnePoints = 0;
        playerTwoPoints = 0;
        playerTwoSets++;
      }

      winner = whoWonTheMatch(playerOneSets, playerTwoSets, bestOf);
    }

    return ResultDto.builder()
        .playerOneScore(playerOnePoints)
        .playerTwoScore(playerTwoPoints)
        .playerOneSets(playerOneSets)
        .playerTwoSets(playerTwoSets)
        .serveSide(Player.PLAYER_ONE)
        .winner(winner)
        .build();
  }

  private Player whoWonTheSet(
      Integer playerOnePoints, Integer playerTwoPoints, Integer totalPointsInSet) {
    if (playerOnePoints >= totalPointsInSet && playerOnePoints > playerTwoPoints + 1) {
      return Player.PLAYER_ONE;
    }
    if (playerTwoPoints >= totalPointsInSet && playerTwoPoints > playerOnePoints + 1) {
      return Player.PLAYER_TWO;
    }
    return Player.NONE;
  }

  private Player whoWonTheMatch(Integer playerOneSets, Integer playerTwoSets, Integer bestOf) {
    if (playerOneSets > Math.floor(bestOf / 2)) {
      return Player.PLAYER_ONE;
    }
    if (playerTwoSets > Math.floor(bestOf / 2)) {
      return Player.PLAYER_TWO;
    }
    return Player.NONE;
  }

  private List<Event> getGameEvents(Integer gameId) {
    String query =
        "SELECT et.event_type FROM game_events ge, event_types et WHERE ge.event_type = et.id AND game_id = "
            + gameId
            + " AND ge.date >= NOW()-INTERVAL 1 HOUR ORDER BY ge.date ASC";
    return jdbcTemplate.queryForList(query, Event.class);
  }

  private Game getGame(Integer gameId) {
    String query = "SELECT player1, player2, bestOf, pointsInSet FROM game WHERE id = " + gameId;
    log.info(query);
    return jdbcTemplate.queryForObject(
        query,
        (resultSet, i) ->
            Game.builder()
                .bestOf(resultSet.getInt("bestOf"))
                .player1(resultSet.getInt("player1"))
                .player2(resultSet.getInt("player2"))
                .pointsInSet(resultSet.getInt("pointsInSet"))
                .build());
  }

  private Integer addEvent(Event event) {
    SimpleJdbcInsert eventsJdbcInsert =
        new SimpleJdbcInsert(mysqlDataSource).withTableName("game_events");

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("game_id", getLatestGameId());
    parameters.put("event_type", event.getId());
    parameters.put("date", new Date());

    log.info("Adding event " + event);
    return eventsJdbcInsert.execute(parameters);
  }

  private Integer getLatestGameId() {
    String query = "SELECT MAX(id) FROM game";
    return jdbcTemplate.queryForObject(query, Integer.class);
  }
}
