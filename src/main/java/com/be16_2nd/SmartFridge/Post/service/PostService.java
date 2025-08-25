package com.be16_2nd.SmartFridge.Post.service;

import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator.FridgeContext;
import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostCategory;
import com.be16_2nd.SmartFridge.Post.domain.PostImage;
import com.be16_2nd.SmartFridge.Post.dto.*;
import com.be16_2nd.SmartFridge.Post.repository.PostCategoryRepository;
import com.be16_2nd.SmartFridge.Post.repository.PostImageRepository;
import com.be16_2nd.SmartFridge.Post.repository.PostRepository;
import com.be16_2nd.SmartFridge.common.service.PostStatsService;
import com.be16_2nd.SmartFridge.common.service.RabbitMqService;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final FridgeAccessValidator fridgeAccessValidator;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PostImageRepository postImageRepository;
    private final PostImageService postImageService;
    private final NotificationService notificationService;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final PostStatsService postStatsService;
    private final RabbitMqService rabbitMqService;

    public Long createPost(Long fridgeId, PostCreateDto createDto) {
        FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();
        Fridge fridge = context.fridge();

        PostCategory postCategory = postCategoryRepository.findById(createDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("없는 카테고리입니다."));

        if ("공지사항".equals(postCategory.getCategory())) {
            if (context.type() != Type.MANAGER) {
                throw new AccessDeniedException("공지사항은 해당 냉장고의 매니저만 작성할 수 있습니다.");
            }
        }

        Post post = Post.builder()
                .title(createDto.getTitle())
                .content(createDto.getContent())
                .member(member)
                .fridge(fridge)
                .category(postCategory)
                .build();
        postRepository.save(post);

        List<String> imageUrls = createDto.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            imageUrls.forEach(url -> {
                PostImage postImage = PostImage.builder().imageUrl(url).post(post).build();
                postImageRepository.save(postImage);
            });
        }

        if (postCategory.getCategory().equals("공지사항")) {
            // 공지사항 등록 알림 (냉장고 관리자 -> 냉장고 참여자)
            List<FridgeMember> fridgeMemberList = fridgeMemberRepository
                    .findAllByFridgeAndType(fridge, Type.COMMON);

            for (FridgeMember fridgeMember : fridgeMemberList) {
                notificationService.create(member, fridgeMember.getMember(),
                        NotificationType.NEW_ANNOUNCEMENT, post);
            }
        }
        return post.getId();
    }

    public Long updatePost(Long fridgeId, Long postId, PostUpdateDto updateDto) {
        fridgeAccessValidator.validate(fridgeId);

        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("없는 게시글입니다"));

        if (!post.getFridge().getId().equals(fridgeId)) {
            throw new AccessDeniedException("해당 냉장고에 속한 게시글이 아닙니다.");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자 입니다."));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        List<String> oldImageUrls = post.getImages().stream().map(PostImage::getImageUrl).collect(Collectors.toList());
        List<String> newImageUrls = updateDto.getImageUrls() != null ? updateDto.getImageUrls() : new ArrayList<>();

        oldImageUrls.stream()
                .filter(url -> !newImageUrls.contains(url))
                .forEach(url -> {
                    postImageService.deleteImage(url);
                    postImageRepository.deleteByPostAndImageUrl(post, url);
                });

        newImageUrls.stream()
                .filter(url -> !oldImageUrls.contains(url))
                .forEach(url -> {
                    PostImage newPostImage = PostImage.builder().imageUrl(url).post(post).build();
                    postImageRepository.save(newPostImage);
                });

        PostCategory postCategory = postCategoryRepository.findById(updateDto.getCategoryId()).orElseThrow(() -> new EntityNotFoundException("없는 카테고리입니다."));
        post.updatePost(updateDto, postCategory);

        return post.getId();
    }

    public void deletePost(Long fridgeId, Long postId) {
        fridgeAccessValidator.validate(fridgeId);

        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("없는 게시글입니다."));

        if (!post.getFridge().getId().equals(fridgeId)) {
            throw new AccessDeniedException("해당 냉장고에 속한 게시글이 아닙니다.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("없는 사용자 입니다."));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        post.getImages().forEach(image -> postImageService.deleteImage(image.getImageUrl()));
        postRepository.delete(post);
    }

    @Transactional
    public PostDetailDto findById(Long fridgeId, Long postId) {
        FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글 없음."));

        if(!post.getFridge().getId().equals(fridgeId)){
            throw new AccessDeniedException("해당 냉장고의 게시글이 아닙니다.");
        }

        postStatsService.incrementViewCount(post.getId());

        Long viewCount = postStatsService.getViewCount(post.getId());
        Long likeCount = postStatsService.getLikeCount(post.getId());

        Boolean likedByUser = postStatsService.isLiked(post.getId(),member.getId());

        rabbitMqService.publishViewUpdate(post.getId());

        return PostDetailDto.fromEntity(post,viewCount,likeCount, likedByUser);
    }

    @Transactional(readOnly = true)
    public Page<PostResDto> findByPosts(Long fridgeId, Pageable pageable, PostSearchDto postSearchDto, String categoryName) {
        fridgeAccessValidator.validate(fridgeId);

        Specification<Post> fridgeSpec = (root, query, cb) -> cb.equal(root.get("fridge").get("id"), fridgeId);

        Specification<Post> searchSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (categoryName != null && !categoryName.equals("전체")) {
                predicates.add(cb.equal(root.get("category").get("category"), categoryName));
            }
            if (postSearchDto.getKeyword() != null && !postSearchDto.getKeyword().isBlank()) {
                String keywordPattern = "%" + postSearchDto.getKeyword() + "%";
                Predicate titleLike = cb.like(root.get("title"), keywordPattern);
                Predicate writerLike = cb.like(root.get("member").get("name"), keywordPattern);
                predicates.add(cb.or(titleLike, writerLike));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Specification<Post> finalSpec = fridgeSpec.and(searchSpec);

        Page<Post> postList = postRepository.findAll(finalSpec, pageable);
        return postList.map(PostResDto::fromEntity);
    }

    public void addLike(Long fridgeId, Long postId) {
        FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글 없음."));

        if (!post.getFridge().getId().equals(fridgeId)) {
            throw new AccessDeniedException("해당 냉장고의 게시글이 아닙니다.");
        }
        postStatsService.addLike(post.getId(), member.getId());
        rabbitMqService.publishLikeUpdate(post.getId(),member.getId());
    }

    public void removeLike(Long fridgeId,Long postId) {
        FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글 없음."));

        if (!post.getFridge().getId().equals(fridgeId)) {
            throw new AccessDeniedException("해당 냉장고의 게시글이 아닙니다.");
        }
        postStatsService.removeLike(post.getId(), member.getId());
        rabbitMqService.publishUnLikeUpdate(post.getId(),member.getId());
    }

    public List<PostCategoryResDto> findAllCategories() {

        List<PostCategory> categories = postCategoryRepository.findAll();
        return categories.stream()
                .map(PostCategoryResDto::fromEntity)
                .collect(Collectors.toList());
    }

}