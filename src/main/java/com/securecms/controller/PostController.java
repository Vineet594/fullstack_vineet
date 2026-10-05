package com.securecms.controller;

import com.securecms.dto.request.PostRequest;
import com.securecms.dto.response.ApiResponse;
import com.securecms.dto.response.PageResponse;
import com.securecms.dto.response.PostResponse;
import com.securecms.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post / Content management with pagination, sorting & N+1 solution")
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "Get all posts (paginated + sorted). Uses JOIN FETCH to avoid N+1")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved", postService.getAllPosts(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID (cached)")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post found", postService.getPostById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new post")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostRequest request) {
        PostResponse created = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update post (owner or Admin)")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Post updated", postService.updatePost(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Delete post (owner or Admin)")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }
}
