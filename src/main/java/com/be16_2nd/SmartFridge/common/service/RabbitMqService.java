package com.be16_2nd.SmartFridge.common.service;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostLike;
import com.be16_2nd.SmartFridge.Post.repository.PostLikeRepository;
import com.be16_2nd.SmartFridge.Post.repository.PostRepository;
import com.be16_2nd.SmartFridge.common.dto.RabbitMqDto;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMqService {
    private final RabbitTemplate rabbitTemplate;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostLikeRepository postLikeRepository;

    public void publishLikeUpdate(Long postId, UUID memberId) {
        RabbitMqDto dto = RabbitMqDto.builder()
                .postId(postId)
                .memberId(memberId)
                .statsType(RabbitMqDto.StatsType.LIKE)
                .build();
        rabbitTemplate.convertAndSend("postLikeQueue", dto);
    }

    public void publishUnLikeUpdate(Long postId, UUID memberId) {
        RabbitMqDto dto = RabbitMqDto.builder()
                .postId(postId)
                .memberId(memberId)
                .statsType(RabbitMqDto.StatsType.UNLIKE)
                .build();
        rabbitTemplate.convertAndSend("postLikeQueue", dto);
    }

    public void publishViewUpdate(Long postId) {
        RabbitMqDto dto = RabbitMqDto.builder()
                .postId(postId)
                .statsType(RabbitMqDto.StatsType.VIEW)
                .build();
        rabbitTemplate.convertAndSend("postViewQueue", dto);
    }


    @RabbitListener(queues = "postViewQueue")
    @Transactional
    public void subscribeViewUpdate(Message message) throws JsonProcessingException {
        String messageBody = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqDto dto = objectMapper.readValue(messageBody, RabbitMqDto.class);
        postRepository.incrementViewCount(dto.getPostId());
    }

    @RabbitListener(queues = "postLikeQueue")
    @Transactional
    public void subscribeLikeUpdate(Message message) throws JsonProcessingException {
        String messageBody = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqDto dto = objectMapper.readValue(messageBody, RabbitMqDto.class);

        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        Post post = postRepository.findById(dto.getPostId()).orElseThrow(() -> new EntityNotFoundException("없는 게시글입니다."));

        if (dto.getStatsType() == RabbitMqDto.StatsType.LIKE) {
            if (!postLikeRepository.existsByMemberAndPost(member, post)) {
                PostLike postLike = PostLike.builder().member(member).post(post).build();
                postLikeRepository.save(postLike);
            }
        } else if (dto.getStatsType() == RabbitMqDto.StatsType.UNLIKE) {
            postLikeRepository.findByMemberAndPost(member, post)
                    .ifPresent(postLikeRepository::delete);
        }
    }
}
