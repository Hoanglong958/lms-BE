package com.ra.base_spring_boot.services.post.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ra.base_spring_boot.dto.Post.PostRequestDTO;
import com.ra.base_spring_boot.dto.Post.PostResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Post;
import com.ra.base_spring_boot.model.Tag;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.PostStatus;
import com.ra.base_spring_boot.repository.post.IPostRepository;
import com.ra.base_spring_boot.repository.post.ITagRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.post.IPostService;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {

    private final IPostRepository postRepository;
    private final ITagRepository tagRepository;
    private final IUserRepository userRepository;

    // ================= CREATE =================
    @Override
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO request) {

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());

        User author = userRepository.findById(
                Objects.requireNonNull(request.getAuthorId(), "authorId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("User không tồn tại"));

        post.setAuthor(author);
        post.setStatus(PostStatus.valueOf(request.getStatus()));
        post.setTags(buildTags(request.getTagNames()));

        return toPostResponseDTO(postRepository.save(post));
    }

    // ================= LIST PUBLISHED =================
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDTO> getPublishedPosts(int page, int size) {

        return postRepository
                .findByStatusWithTags(
                        PostStatus.PUBLISHED,
                        PageRequest.of(page, size))
                .map(this::toPostResponseDTO);
    }

    // ================= LIST DRAFTS =================
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDTO> getDraftPosts(int page, int size) {

        return postRepository
                .findByStatusWithTags(
                        PostStatus.DRAFT,
                        PageRequest.of(page, size))
                .map(this::toPostResponseDTO);
    }

    // ================= DETAIL =================
    @Override
    @Transactional(readOnly = true)
    public PostResponseDTO getPostById(Long id) {

        Post post = postRepository.findPostDetailById(id)
                .orElseThrow(() -> new HttpBadRequest("Post không tồn tại"));

        return toPostResponseDTO(post);
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public PostResponseDTO updatePost(Long id, PostRequestDTO request) {

        Post post = postRepository.findPostDetailById(id)
                .orElseThrow(() -> new HttpBadRequest("Post không tồn tại"));

        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setStatus(PostStatus.valueOf(request.getStatus()));
        post.setTags(buildTags(request.getTagNames()));

        return toPostResponseDTO(postRepository.save(post));
    }

    // ================= DELETE =================
    @Override
    @Transactional
    public void deletePost(Long id) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Post không tồn tại"));

        postRepository.delete(post);
    }

    // ================= DTO MAPPER =================
    private PostResponseDTO toPostResponseDTO(Post post) {

        PostResponseDTO.AuthorResponse authorDTO = PostResponseDTO.AuthorResponse.builder()
                .id(post.getAuthor().getId())
                .fullName(post.getAuthor().getFullName())
                .build();

        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .author(authorDTO)
                .tags(post.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .build();
    }

    // ================= SEARCH =================
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDTO> searchPosts(String q, int page, int size) {
        String keyword = q == null ? "" : q.trim();
        return postRepository.findByTitleContainingIgnoreCaseAndStatus(
                keyword,
                PostStatus.PUBLISHED,
                PageRequest.of(page, size)).map(this::toPostResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDTO> searchPostsAdvanced(String q, String tagName, String status, int page, int size,
            String sort) {
        Sort sortObj;
        String[] sortParts = sort.split(",");
        if (sortParts.length == 2) {
            sortObj = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Specification<Post> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by status. Default to PUBLISHED if not specified.
            if (status == null || status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), PostStatus.PUBLISHED));
            } else if (!status.equalsIgnoreCase("ALL")) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("status"), PostStatus.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    // Invalid status, fall back to PUBLISHED for security
                    predicates.add(criteriaBuilder.equal(root.get("status"), PostStatus.PUBLISHED));
                }
            }

            if (q != null && !q.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + q.trim().toLowerCase() + "%"));
            }

            if (tagName != null && !tagName.trim().isEmpty() && !tagName.equals("Tất cả")) {
                Join<Post, Tag> tagsJoin = root.join("tags", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(tagsJoin.get("name"), tagName.trim()));
            }

            // Ensure distinct results because of joins
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return postRepository.findAll(spec, PageRequest.of(page, size, sortObj))
                .map(this::toPostResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return tagRepository.findAll().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    // ================= TAG HELPER =================
    private Set<Tag> buildTags(List<String> tagNames) {

        Set<Tag> tags = new HashSet<>();

        if (tagNames != null) {
            for (String name : tagNames) {
                Tag tag = tagRepository.findByName(name);
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(name);
                    tagRepository.save(tag);
                }
                tags.add(tag);
            }
        }
        return tags;
    }
}
