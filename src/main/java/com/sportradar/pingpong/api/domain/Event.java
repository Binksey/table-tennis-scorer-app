package com.sportradar.pingpong.api.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Event {
  POINT_PLAYER_ONE(1),
  POINT_PLAYER_TWO(2),
  UNDO_POINT_PLAYER_ONE(3),
  UNDO_POINT_PLAYER_TWO(4);

  @Getter private final Integer id;
}
