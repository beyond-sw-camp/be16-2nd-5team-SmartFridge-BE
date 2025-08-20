package com.be16_2nd.SmartFridge.Post.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class PostCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @OneToMany(mappedBy = "category")
    private List<Post> posts = new ArrayList<>();
}
