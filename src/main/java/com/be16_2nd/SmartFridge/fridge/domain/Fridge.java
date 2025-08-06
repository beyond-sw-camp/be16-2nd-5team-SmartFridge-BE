package com.be16_2nd.SmartFridge.fridge.domain;

import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Fridge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fridgeId;
    @Column(nullable = false)
    private String fridgeName;
    private String description;

    @Column(unique = true)
    private String inviteCode;

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL)
    @Builder.Default
    List<FridgeMember> fridgeMemberList = new ArrayList<>();

}

