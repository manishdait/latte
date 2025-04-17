package com.example.latte_api.comment;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.comment.dto.CommentRequest;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/latte-api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @PostMapping()
  public ResponseEntity<ActivityDto> createComment(@RequestBody CommentRequest request, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(request, authentication));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ActivityDto> updateComment(@PathVariable Long id, @RequestBody CommentRequest request, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.OK).body(commentService.updateComment(id, request, authentication));
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Boolean>> deleteComment(@PathVariable Long id, Authentication authentication) {
    commentService.deleteComment(id, authentication);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("deleted", true));
  }
}
