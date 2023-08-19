package com.popple.server.domain.board.controller;

import com.popple.server.common.dto.APIDataResponse;
import com.popple.server.domain.board.dto.*;
import com.popple.server.domain.board.service.BoardService;
import com.popple.server.domain.entity.Member;
import com.popple.server.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RequestMapping("/api/board")
@RestController
@RequiredArgsConstructor
@Slf4j
public class BoardController {
    private final BoardService boardService;

    //전체 게시글
    @GetMapping("/all")
    public BoardAPIDataResponse<List<BoardListRespDto>> getAllPosts() {
        //서비스 메서드 호출
        List<Post> posts = boardService.getAllPosts();
        List<BoardListRespDto> boardListRespDtoList = createListOfBoardListRespDto(posts);
        return BoardAPIDataResponse.of(HttpStatus.OK, boardListRespDtoList, (long) posts.size());
    }

    @GetMapping()
    public BoardAPIDataResponse<List<BoardListRespDto>> getPostsByPage(@PageableDefault Pageable pageable) {
        Page<Post> postsByPage = boardService.getPostsByPage(pageable);
        List<Post> contents = postsByPage.getContent();
        List<BoardListRespDto> boardListRespDtoList = createListOfBoardListRespDto(contents);
        return BoardAPIDataResponse.of(HttpStatus.OK, boardListRespDtoList, postsByPage.getTotalElements());
    }

    @GetMapping("/{postId}")
    public APIDataResponse<PostRespDto> getPostById(@PathVariable Long postId) {
        try {
            Post post = boardService.getPostById(postId);
            List<CommentDto> commentDtos = boardService.getAllCommentsByPostId(postId);
            PostRespDto postRespDto = PostRespDto.builder()
                    .id(post.getId())
                    .nickname(post.getMember().getNickname())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .comments(commentDtos)
                    .build();
            return APIDataResponse.of(HttpStatus.OK, postRespDto);
        } catch (NoSuchElementException e) {
            //Error응답
            log.error(e.getMessage());
        }
        return null;
    }

    @PostMapping("/write")
    public APIDataResponse<?> savePost(PostReqDto postReqDto, BindingResult bindingResult) {
        Member member = boardService.findMemberByEmail(postReqDto.getEmail());
        Post post = postReqDto.toEntity(member);
        boardService.savePost(post);
        return APIDataResponse.of(HttpStatus.OK, null);
    }

    private List<BoardListRespDto> createListOfBoardListRespDto(List<Post> posts) {
        List<BoardListRespDto> boardListRespDtoList = new ArrayList<>();
        for (Post post : posts) {
            int commentCount = boardService.getAllCommentsByPostId(post.getId()).size();
            BoardListRespDto boardListRespDto = BoardListRespDto.builder()
                    //Todo builder사용하는 코드 -> 객체 내부에 정의해 코드 라인 수 줄이기
                    .id(post.getId())
                    .nickname(post.getMember().getNickname())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .commentCount(commentCount)
                    .build();
            boardListRespDtoList.add(boardListRespDto);
        }
        return boardListRespDtoList;
    }

    @DeleteMapping("/{postId}")
    public APIDataResponse<?> deletePost(@PathVariable Long postId) throws IllegalArgumentException {
        boardService.deletePost(postId);
        return APIDataResponse.empty(HttpStatus.OK);
    }
}
