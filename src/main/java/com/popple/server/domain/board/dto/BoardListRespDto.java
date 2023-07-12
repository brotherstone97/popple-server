package com.popple.server.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Todo 전체 리스트 조회 구현 후 페이지네이션 구현
public class BoardListRespDto {
    private Long id;
    private String nickname;
    private String title;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int commentCount;
}
