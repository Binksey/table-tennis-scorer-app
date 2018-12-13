package com.sportradar.pingpong.api.domain;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Game {
  private final Integer player1;
  private final Integer player2;
  private final Integer bestOf;
  private final Integer pointsInSet;
}
