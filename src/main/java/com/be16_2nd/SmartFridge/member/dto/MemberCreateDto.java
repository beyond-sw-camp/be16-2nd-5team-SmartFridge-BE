package com.be16_2nd.SmartFridge.member.dto;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberCreateDto {
    @NotEmpty(message = "이름은 필수 입력 항목입니다.")
    private String name;
    @NotEmpty(message = "이메일은 필수 입력 항목입니다.")
    private String email;
    @NotEmpty(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 16)
    private String password;
    @NotEmpty(message = "비밀번호확인란은 필수 입력 항목입니다.")
    @Size(min = 8, max = 16)
    private String passwordConfirm;

    public Member toEntity(String encodedPassword){
        return Member.builder()
                .name(this.name)
                .password(encodedPassword)
                .email(this.email)
                .role(Role.USER)
//                .socialType()
//                .profileImage() //nullable 하게 설계해서 oauth인증방식으로 로그인하면 사진 가져오는 형식
                .build();
    }
}
