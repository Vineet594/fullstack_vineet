package com.securecms.service;

import com.securecms.dto.request.PostRequest;
import com.securecms.dto.response.PageResponse;
import com.securecms.dto.response.PostResponse;
import com.securecms.entity.Category;
import com.securecms.entity.Post;
import com.securecms.entity.User;
import com.securecms.exception.ResourceNotFoundException;
import com.securecms.exception.UnauthorizedException;
import com.securecms.repository.CategoryRepository;
import com.securecms.repository.PostRepository;
import com.securecms.repository.UserRepository;
import com.securecms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Uses EntityGraph (JOIN FETCH) to solve N+1 problem.
     * One query fetches posts + author + category.
     */
    public PageResponse<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> page = postRepository.findAllWithAuthorAndCategory(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Cacheable(value = "post", key = "#id")
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findByIdWithAuthorAndCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return toResponse(post);
    }

    @Transactional
    @CacheEvict(value = "post", allEntries = true)
    public PostResponse createPost(PostRequest request) {
        UserPrincipal principal = getCurrentUser();
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .category(category)
                .build();

        return toResponse(postRepository.save(post));
    }

    @Transactional
    @CacheEvict(value = "post", key = "#id")
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findByIdWithAuthorAndCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        checkOwnershipOrAdmin(post);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(category);

        return toResponse(postRepository.save(post));
    }

    @Transactional
    @CacheEvict(value = "post", key = "#id")
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        checkOwnershipOrAdmin(post);
        postRepository.delete(post);
    }

    private void checkOwnershipOrAdmin(Post post) {
        UserPrincipal principal = getCurrentUser();
        boolean isAdmin = "ADMIN".equals(principal.getRole());
        boolean isOwner = post.getAuthor().getId().equals(principal.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("You can only modify your own posts");
        }
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private PostResponse toResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthor().getId())
                .authorUsername(post.getAuthor().getUsername())
                .categoryId(post.getCategory().getId())
                .categoryName(post.getCategory().getName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
