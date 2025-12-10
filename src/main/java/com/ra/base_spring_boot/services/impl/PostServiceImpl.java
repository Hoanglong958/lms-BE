    package com.ra.base_spring_boot.services.impl;

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
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    public class PostServiceImpl implements IPostService {

        private final IPostRepository postRepository;
        private final ITagRepository tagRepository;
        private final IUserRepository userRepository;

        @Override
        @Transactional
        public PostResponseDTO createPost(PostRequestDTO request) {
            Post post = new Post();
            post.setTitle(request.getTitle());
            post.setSlug(request.getSlug());
            post.setContent(request.getContent());

            // set author
            User author = userRepository.findById(java.util.Objects.requireNonNull(request.getAuthorId(), "authorId must not be null"))
                    .orElseThrow(() -> new HttpBadRequest("User không tồn tại"));
            post.setAuthor(author);

            // set status
            post.setStatus(PostStatus.valueOf(request.getStatus()));

            // set tags
            Set<Tag> tags = buildTags(request.getTagNames());
            post.setTags(tags);

            Post saved = postRepository.save(post);
            return toPostResponseDTO(saved);
        }

        @Override
        public Page<PostResponseDTO> getPublishedPosts(int page, int size) {
            Page<Post> posts = postRepository.findByStatus(PostStatus.PUBLISHED, PageRequest.of(page, size));
            return posts.map(this::toPostResponseDTO);
        }

        @Override
        public PostResponseDTO getPostById(Long id) {
            Post post = postRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Post không tồn tại"));
            return toPostResponseDTO(post);
        }

        @Override
        @Transactional
        public PostResponseDTO updatePost(Long id, PostRequestDTO request) {
            Post post = postRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Post không tồn tại"));

            post.setTitle(request.getTitle());
            post.setSlug(request.getSlug());
            post.setContent(request.getContent());
            post.setStatus(PostStatus.valueOf(request.getStatus()));

            // update tags
            Set<Tag> tags = buildTags(request.getTagNames());
            post.setTags(tags);

            Post saved = postRepository.save(post);
            return toPostResponseDTO(saved);
        }

        @Override
        @Transactional
        public void deletePost(Long id) {
            if(!postRepository.existsById(java.util.Objects.requireNonNull(id, "id must not be null"))) {
                throw new HttpBadRequest("Post không tồn tại");
            }
            postRepository.deleteById(java.util.Objects.requireNonNull(id, "id must not be null"));
        }

        // helper: entity -> DTO
        private PostResponseDTO toPostResponseDTO(Post post) {
            PostResponseDTO response = new PostResponseDTO();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setSlug(post.getSlug());
            response.setContent(post.getContent());
            response.setStatus(post.getStatus().name());
            response.setCreatedAt(post.getCreatedAt());

            // author
            PostResponseDTO.AuthorResponse authorDTO = new PostResponseDTO.AuthorResponse();
            authorDTO.setId(post.getAuthor().getId());
            authorDTO.setFullName(post.getAuthor().getFullName());
            response.setAuthor(authorDTO);

            // tags
            List<String> tags = post.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList());
            response.setTags(tags);

            return response;
        }

        // helper: build Set<Tag> từ list tên tag, tạo mới nếu chưa tồn tại
        private Set<Tag> buildTags(List<String> tagNames) {
            Set<Tag> tags = new HashSet<>();
            if(tagNames != null) {
                for(String name : tagNames) {
                    Tag tag = tagRepository.findByName(name);
                    if(tag == null) {
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
