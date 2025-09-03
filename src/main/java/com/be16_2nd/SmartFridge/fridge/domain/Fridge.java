package com.be16_2nd.SmartFridge.fridge.domain;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.chat.domain.ManagerChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.food.dto.FoodUpdateDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeUpdateDto;
import com.be16_2nd.SmartFridge.notification.domain.Notification;
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
public class Fridge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fridgeName;
    private String description;

    @Column(unique = true)
    private String inviteCode;

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL)
    @Builder.Default
    List<FridgeMember> fridgeMemberList = new ArrayList<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foods = new ArrayList<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ManagerChatRoom>  managerChatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseChatRoom>  purchaseChatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    public void updateFridge(FridgeUpdateDto fridgeUpdateDto) {
        this.fridgeName = fridgeUpdateDto.getFridgeName();
        this.description = fridgeUpdateDto.getDescription();
    }

}

