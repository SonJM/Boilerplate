package kr.co.sonjm.demo.feature.post.controller;

import kr.co.sonjm.demo.global.annotation.LogExecutionTime;
import kr.co.sonjm.demo.global.common.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

	@LogExecutionTime
	@GetMapping
	public ResponseEntity<CommonResponse> getPosts() {
		return ResponseEntity.ok(null);
	}
}
