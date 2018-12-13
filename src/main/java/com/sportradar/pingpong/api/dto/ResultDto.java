package com.sportradar.pingpong.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sportradar.pingpong.api.domain.Player;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonSerialize
public class ResultDto {
  private Integer playerOneScore;
  private Integer playerTwoScore;
  private Integer playerOneSets;
  private Integer playerTwoSets;
  private Player winner;
  private Player serveSide;
}
