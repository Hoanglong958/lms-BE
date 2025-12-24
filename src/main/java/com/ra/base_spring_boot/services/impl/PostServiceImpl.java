package com.ra.base_spring_boot.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ra.base_spring_boot.dto.Post.PostRequestDTO;
import com.ra.base_spring_boot.dto.Post.PostResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Post;
import com.ra.base_spring_boot.model.Tag;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.PostStatus;
import com.ra.base_spring_boot.repository.IPostRepository;
import com.ra.base_spring_boot.repository.ITagRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IPostService;

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
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .author(authorDTO)
                .tags(post.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .build();
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
