package com.be16_2nd.SmartFridge.Post.dto;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostCategory;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCreateDto {
    private String title;
    private String content;
    private Long categoryId;
    private List<String> imageUrls;

//    public Post toEntity(Member member, Fridge fridge, PostCategory postCategory){
//        return Post.builder()
//                .title(this.title)
//                .content(this.content)
//                .category(postCategory)
//                .member(member)
//                .fridge(fridge)
//                .build();
//    }
}
