package com.securecms.repository;

import com.securecms.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Solves N+1 problem using EntityGraph (JOIN FETCH under the hood).
     * Fetches author and category in a single query.
     */
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT p FROM Post p")
    Page<Post> findAllWithAuthorAndCategory(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    java.util.Optional<Post> findByIdWithAuthorAndCategory(Long id);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
}
