# 스마트 냉장고: 공용 냉장고 관리 웹 서비스

<div align="left">
  <img src="https://cdn.discordapp.com/attachments/1386519346293112972/1409777259983667230/image.png?ex=68ae9ce3&is=68ad4b63&hm=dd69c7da78d11a8ad7f81a7af192bb618700e1b8ee175619885ee15d05ba4b8c&" alt="Smart Fridge Project Banner" width="800"/>
</div>

<br/>

## 🙋🏻 팀원 소개

| **김영관** | **김지현** | **윤세진** | **김찬진** |
| :---: | :---: | :---: | :---: |
| [<img src="https://avatars.githubusercontent.com/u/102599602?v=4" height=150 width=150> <br/> @YoungKwanK](https://github.com/YoungKwanK) | [<img src="https://avatars.githubusercontent.com/u/97399433?v=4" height=150 width=150> <br/> @Jihyeon0804](https://github.com/Jihyeon0804) | [<img src="https://avatars.githubusercontent.com/u/163013233?v=4" height=150 width=150> <br/> @AstroJini](https://github.com/AstroJini) | [<img src="https://avatars.githubusercontent.com/u/151761821?v=4" height=150 width=150> <br/> @Chanjin629](https://github.com/Chanjin629) |

<br/>

---

## 💡 프로젝트 기획

### **프로젝트 한 줄 소개**
공용 냉장고 청결과 사용자 소통을 돕는 음식 등록·관리 웹 서비스

<details>
<summary><strong>주제 선정 배경</strong></summary>
<br>
<ul>
    <li>
        <strong>공동 냉장고 사용 시 식자재 관리의 어려움</strong>
        <ul>
            <li>여러 사람이 함께 사용하는 냉장고에서는 누가 어떤 식재료를 넣었는지, 유통기한이 언제인지 파악하기 어렵습니다.</li>
            <li>유통기한이 지난 음식물이 방치되거나, 중복 구입으로 인한 자원 낭비가 빈번히 발생합니다.</li>
        </ul>
    </li>
    <li>
        <strong>책임 소재 불분명으로 인한 불편</strong>
        <ul>
            <li>음식이 상하거나 냄새가 날 경우, 누구의 물건인지 몰라 처리하지 못하고 갈등이 생깁니다.</li>
            <li>공동 냉장고의 청결 및 관리 문제로 생활의 질이 저하됩니다.</li>
        </ul>
    </li>
    <li>
        <strong>기록 부재로 인한 비효율</strong>
        <ul>
            <li>메모지나 마커 등 수기로 남기는 방식은 지속되기 어렵고, 정보 공유가 제한적입니다.</li>
            <li>디지털 시스템을 통해 실시간 등록, 조회, 유통기한 알림이 가능한 관리 시스템이 필요합니다.</li>
        </ul>
    </li>
    <li>
        <strong>소규모 공동체 내 자원 순환 및 협력 문화 조성</strong>
        <ul>
            <li>남은 재료를 등록하고 필요한 사람이 활용하는 환경을 조성하여 식자재 낭비를 줄이고 상호 협력을 강화합니다.</li>
        </ul>
    </li>
</ul>
</details>

<details>
<summary><strong>기대 효과</strong></summary>
<br>
<ul>
    <li>
        <strong>냉장고 관리 효율성 향상</strong>
        <ul>
            <li>식자재의 등록, 조회, 삭제를 웹에서 간편하게 처리하여 유통기한, 수량 등을 한눈에 확인할 수 있습니다.</li>
            <li>불필요한 중복 구매 및 음식물 방치를 방지하고, 관리자의 공지사항 기능으로 사용 규칙을 쉽게 전파할 수 있습니다.</li>
        </ul>
    </li>
    <li>
        <strong>유통기한 기반 자동 알림으로 낭비 감소</strong>
        <ul>
            <li>유통기한 임박 시 알림 기능을 통해 음식물 폐기를 줄이고 자원의 낭비를 최소화합니다.</li>
        </ul>
    </li>
    <li>
        <strong>책임소재 명확화로 갈등 감소</strong>
        <ul>
            <li>사용자별로 등록된 식자재 정보를 관리하여, '이건 누구 음식?'과 같은 문제를 해결하고 불필요한 갈등을 예방합니다.</li>
        </ul>
    </li>
    <li>
        <strong>자원 순환 및 공유 문화 확산</strong>
        <ul>
            <li>사용하지 않는 식자재를 공유함으로써 자원 재사용을 촉진하고, ‘냉털(냉장고 털기)’ 문화 정착에 기여합니다.</li>
        </ul>
    </li>
    <li>
        <strong>사용자 간 채팅 기능으로 소통 강화</strong>
        <ul>
            <li>식자재 공유 여부, 사용 문의 등을 위한 채팅 기능으로 협업 및 관리 효율을 증대합니다.</li>
            <li>예: "이 우유 써도 될까요?", "오늘까지 먹어야 하는 김밥 있어요" 등의 대화로 소통을 원활하게 합니다.</li>
        </ul>
    </li>
</ul>
</details>

---

## 🛠️ 기술 스택

### **데이터베이스**
![MariaDB](https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Amazon S3](https://img.shields.io/badge/amazons3-569A31?style-for-the-badge&logo=amazons3&logoColor=white)

### **백엔드**
![Java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white)
![STOMP](https://img.shields.io/badge/STOMP-000000?style=for-the-badge)

### **프론트엔드**
![Vue.js](https://img.shields.io/badge/vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Vuetify](https://img.shields.io/badge/vuetify-1867C0?style=for-the-badge&logo=vuetify&logoColor=white)

### **도구**
![Notion](https://img.shields.io/badge/notion-181717?style=for-the-badge&logo=notion&logoColor=white)
![Git](https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white)
![Figma](https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)

---

## ✨ 주요 기능 및 기술 상세

| 기술 | 설명 |
| :--- | :--- |
| **JWT (JSON Web Token)** | Stateless 환경의 API 보안을 위해 표준 기술인 JWT를 도입했습니다. 세션 방식과 달리 서버의 상태를 저장할 필요가 없어 **수평적 확장에 유리**하며, 경로별 접근 권한을 중앙에서 관리하여 **유지보수 효율을 높였습니다.** |
| **WebSocket & STOMP** | 사용자 간의 즉각적인 소통을 위해 WebSocket 기반의 실시간 채팅 기능을 구현했습니다. STOMP 프로토콜을 함께 사용하여 메시지 목적지를 명확히 하는 **발행/구독 모델을 적용**, 채팅 로직을 단순화하고 **안정적인 통신**이 가능하도록 했습니다. |
| **SSE (Server-Sent Events)** | 유통기한 임박과 같이 서버에서 사용자에게만 정보를 전달해야 하는 상황에 SSE를 사용했습니다. 양방향 통신이 필요한 WebSocket보다 **가볍고 서버 리소스를 효율적으로 사용**하여, 이벤트 타입별 개별 처리 및 알림 카운트가 반영되는 **안정적인 알림 시스템**을 구축했습니다. |
| **RabbitMQ** | 좋아요/조회수의 DB 업데이트 과정에서 발생하는 **동시성 문제를 제어**하기 위해 도입했습니다. 수많은 요청을 메시지 큐에 순서대로 쌓아두고 하나씩 직렬로 처리하여, **DB 락(Lock) 경합을 막고 시스템을 안정적으로 유지**합니다. |
| **Redis** | 인메모리 저장소인 Redis를 다목적으로 활용하여 서비스 성능을 최적화했습니다.<br>• **좋아요/조회수**: 빈번한 쓰기(Write) 작업을 메모리에서 **원자적 연산(Atomic Operation)**으로 처리하여 DB 부하를 줄이고 매우 빠른 응답 속도를 보장합니다.<br>• **실시간 통신 (Pub/Sub)**: 여러 서버 간의 채팅 메시지와 알림을 중개하는 **메시지 브로커** 역할을 수행하여, 사용자의 접속 서버와 관계없이 안정적인 실시간 통신을 가능하게 합니다.<br>• **이메일 인증**: 인증코드에 **유효 시간(TTL)을 설정**하여 재사용을 방지하고 보안을 강화했습니다. |
| **OAuth 2.0 Social Login** | 사용자 편의성을 높이기 위해 소셜 로그인을 도입하되, 라이브러리에 의존하지 않고 표준 OAuth 2.0 명세를 직접 구현했습니다. 이를 통해 **기술에 대한 깊은 이해를 확보**하고, 향후 다른 소셜 로그인 제공자를 추가할 때 **유연하게 확장**할 수 있는 기반을 마련했습니다. |
| **SMTP (Simple Mail Transfer Protocol)** | JavaMail 라이브러리를 활용하여 사용자에게 인증코드를 담은 HTML 형식의 이메일을 발송합니다. SMTP의 *텍스트 기반 명령어(MAIL FROM, RCPT TO, DATA)*와 원자적 세션 처리, 인증(AUTH) 및 TLS/SSL 암호화 기능을 적용하여, 이메일 전송 과정에서의 **보안성과 신뢰성을 확보**했습니다. |
| **AWS S3** | 사용자가 업로드하는 이미지 파일들을 안정적으로 관리하기 위해 AWS S3를 사용했습니다. 서버의 스토리지 부담을 없애 **핵심 로직에만 집중**할 수 있게 하고, 향후 트래픽 증가에 따른 **파일 저장 공간 확장에 유연하게 대비**했습니다. |

---

## 📋 프로젝트 산출물

| 구분 | 링크 |
| :--- | :--- |
| **Figma** | [🔗 Figma 디자인 보기](https://www.figma.com/design/wU7s6yvtkBz0H1dKfz3wwH/SmartFridge?node-id=889-2310&p=f&t=I5OoZS4LQj3x4ExS-0) |
| **API 명세서** | [🔗 API 명세서 보기](https://docs.google.com/spreadsheets/d/1nH7eOviqjvMAsm5lpgn3xR_aUZIRZFVX/edit?gid=2135429763#gid=2135429763) |
| **WBS** | [🔗 WBS 보기](https://docs.google.com/spreadsheets/d/1MdMci2yeFGn26Q4miN4VBFLd0V3hfnp7/edit?gid=1981675247#gid=1981675247) |
| **요구사항 명세서** | [🔗 요구사항 명세서 보기](https://docs.google.com/spreadsheets/d/1VasDKfhetE3DzFZJidi0ld-xMtJ5dCzDggyUzre7fkc/edit?gid=0#gid=0) |

<br/>

## 💾 ERD
<img width="100%" alt="SmartFridge ERD" src="https://github.com/user-attachments/assets/e36c9e41-28f6-4ea1-8cc4-a41287fa6f27" />

---

## 테스트 결과물

<details>
<summary><strong>👤 회원 관리</strong></summary>
<br>

<details>
<summary><strong>회원가입 (이메일 인증)</strong></summary>
<br>
<p><strong>- 회원가입 정보 입력 및 인증코드 발송</strong></p>
<img width="100%" alt="회원가입 정보 입력" src="https://github.com/user-attachments/assets/d2717090-b9b8-4be7-8c62-0e3e9c15cb8d" />
<p><strong>- 이메일로 수신된 인증코드 확인</strong></p>
<img width="100%" alt="인증코드 이메일" src="https://github.com/user-attachments/assets/9ea0b99f-a803-432d-9042-948d78493003" />
<p><strong>- 인증코드 입력 및 검증 완료</strong></p>
<img width="100%" alt="이메일 인증 완료" src="https://github.com/user-attachments/assets/c30bb937-4d71-4771-b390-08ad4e9f4bc0" />
</details>

<details>
<summary><strong>로그인</strong></summary>
<br>
<img width="100%" alt="로그인 전" src="https://github.com/user-attachments/assets/4019ee07-2849-41ec-87c6-332a3700fc6b" />
<img width="100%" alt="로그인 후" src="https://github.com/user-attachments/assets/93eb1c1a-4c1a-43b0-b28c-c899bf597f39" />
</details>

<details>
<summary><strong>소셜 로그인 (Google, Kakao, Naver)</strong></summary>
<br>
<p><strong>- Google</strong></p>
<img width="100%" alt="구글 로그인" src="https://github.com/user-attachments/assets/d7763fd0-e508-4864-9157-28488cf35a79" />
<p><strong>- Kakao</strong></p>
<img width="100%" alt="카카오 로그인" src="https://github.com/user-attachments/assets/98c35f73-63f5-4154-af31-6dc5b45520d3" />
<p><strong>- Naver</strong></p>
<img width="100%" alt="네이버 로그인" src="https://github.com/user-attachments/assets/e522d2e7-39ff-4e02-b2f8-bded27100ef6" />
</details>

<details>
<summary><strong>마이페이지 (알림 설정)</strong></summary>
<br>
<img width="100%" alt="마이페이지" src="https://github.com/user-attachments/assets/b2500217-4f2d-4066-be20-e6b801bc9f2b" />
<img width="100%" alt="알림 설정" src="https://github.com/user-attachments/assets/6cdbd3f0-186e-4a22-ba38-62e0be0a59e2" />
</details>

</details>

<br>

<details>
<summary><strong>🧊 냉장고 관리</strong></summary>
<br>

<details>
<summary><strong>냉장고 생성</strong></summary>
<br>
<img width="100%" alt="냉장고 생성 1" src="https://github.com/user-attachments/assets/cc1f5af3-0b8f-4300-9047-c087449b345b" />
<img width="100%" alt="냉장고 생성 2" src="https://github.com/user-attachments/assets/6fc53353-abaf-4eef-9e2a-9ef1e7332fb7" />
</details>

<details>
<summary><strong>냉장고 수정</strong></summary>
<br>
<img width="100%" alt="냉장고 수정 1" src="https://github.com/user-attachments/assets/42be1304-bf03-45d7-8c97-628c35820fe8" />
<img width="100%" alt="냉장고 수정 2" src="https://github.com/user-attachments/assets/fd34d7c9-78d1-4c21-ba59-286bce1f1687" />
</details>

<details>
<summary><strong>냉장고 삭제</strong></summary>
<br>
<img width="100%" alt="냉장고 삭제 1" src="https://github.com/user-attachments/assets/dd31f5e6-905b-43a0-9d5d-4015969fd011" />
<img width="100%" alt="냉장고 삭제 2" src="https://github.com/user-attachments/assets/0ec11178-55a1-4c0c-91cc-00877f9f9e96" />
</details>

<details>
<summary><strong>냉장고 참여</strong></summary>
<br>
<img width="100%" alt="냉장고 참여 1" src="https://github.com/user-attachments/assets/8846fb91-5082-4474-8b99-762e5fad327d" />
<img width="100%" alt="냉장고 참여 2" src="https://github.com/user-attachments/assets/9a3a3bd9-a079-46ac-9c14-bec2da216ebd" />
</details>

<details>
<summary><strong>냉장고 나가기</strong></summary>
<br>
<img width="100%" alt="냉장고 나가기 1" src="https://github.com/user-attachments/assets/0d352088-53f3-4632-8d0a-8f4a50b5232a" />
<img width="100%" alt="냉장고 나가기 2" src="https://github.com/user-attachments/assets/065d36c0-ecb9-47ba-ab30-6f5697b8a7a7" />
</details>

<details>
<summary><strong>대시보드</strong></summary>
<br>
<img width="100%" alt="냉장고 대시보드" src="https://github.com/user-attachments/assets/2998b3ba-d011-4d42-9bbe-cafb667f47aa" />
</details>

</details>

<br>

<details>
<summary><strong>🥕 식품 관리</strong></summary>
<br>

<details>
<summary><strong>식품 등록</strong></summary>
<br>
<img width="100%" alt="식품 등록 1" src="https://github.com/user-attachments/assets/d9ca7291-cc8c-40d1-b293-516ad0fd24ca" />
<img width="100%" alt="식품 등록 2" src="https://github.com/user-attachments/assets/d04dd39c-626a-463b-b7ac-61cfd49b27e2" />
</details>

<details>
<summary><strong>식품 수정</strong></summary>
<br>
<img width="100%" alt="식품 수정 1" src="https://github.com/user-attachments/assets/d9908272-7581-4e44-baa1-4be70e572ba9" />
<img width="100%" alt="식품 수정 2" src="https://github.com/user-attachments/assets/1d14ce54-26cd-4377-be80-efbbf4b58051" />
</details>

<details>
<summary><strong>식품 삭제</strong></summary>
<br>
<img width="100%" alt="식품 삭제 1" src="https://github.com/user-attachments/assets/85f4185b-5611-4075-abdb-d5b685de4d45" />
<img width="100%" alt="식품 삭제 2" src="https://github.com/user-attachments/assets/10019246-0af1-4198-865d-6a4db9ce1659" />
</details>

</details>

<br>

<details>
<summary><strong>✍️ 게시판</strong></summary>
<br>
<details>
<summary><strong>게시글 등록</strong></summary>
<br>
<img width="100%" alt="게시글 등록 1" src="https://github.com/user-attachments/assets/e1a4856d-2404-4e0d-a0d4-ce4007155cea" />
<img width="100%" alt="게시글 등록 2" src="https://github.com/user-attachments/assets/cf7cf218-74e9-4a22-9e2a-131263829a58" />
<img width="100%" alt="게시글 등록 3" src="https://github.com/user-attachments/assets/94822f13-85dd-482c-953b-c583eb98a268" />
</details>
</details>

<br>

<details>
<summary><strong>💬 채팅</strong></summary>
<br>
<details>
<summary><strong>내 채팅방 목록 조회</strong></summary>
<br>
<img width="100%" alt="채팅방 목록" src="https://github.com/user-attachments/assets/c497dfbf-eef4-4aac-98f0-5fd30c7f4db9" />
</details>
<details>
<summary><strong>채팅방 생성 및 참여</strong></summary>
<br>
<img width="100%" alt="채팅방 생성" src="https://github.com/user-attachments/assets/f42d1eb1-0769-4bbd-a72d-e33a051cf1fa" />
</details>
<details>
<summary><strong>메시지 전송 (텍스트/이미지)</strong></summary>
<br>
<p><strong>- 텍스트 메시지</strong></p>
<img width="100%" alt="텍스트 메시지 전송" src="https://github.com/user-attachments/assets/ae3cf813-e5fd-4154-9092-be776442e318" />
<p><strong>- 이미지 메시지</strong></p>
<img width="100%" alt="이미지 메시지 전송" src="https://github.com/user-attachments/assets/2fcae934-ebc8-4532-97e6-a917a2027343" />
</details>
<details>
<summary><strong>채팅방 나가기</strong></summary>
<br>
<img width="100%" alt="채팅방 나가기" src="https://github.com/user-attachments/assets/f1287850-c81e-453d-af4f-cfcf56b41091" />
</details>
</details>

<br>

<details>
<summary><strong>❓ 문의</strong></summary>
<br>
<details>
<summary><strong>문의 목록 조회</strong></summary>
<br>
<img width="100%" alt="문의 목록" src="https://github.com/user-attachments/assets/645cd71c-762f-402f-89f2-82dbc6cd65dc" />
</details>
<details>
<summary><strong>문의 등록</strong></summary>
<br>
<img width="100%" alt="문의 등록" src="https://github.com/user-attachments/assets/6b85de17-9b70-41f9-bb1f-7eee0e827032" />
</details>
</details>

<br>

<details>
<summary><strong>🔔 알림</strong></summary>
<br>

<details>
<summary><strong>알림 목록 조회</strong></summary>
<br>
<img width="100%" alt="알림 목록(전체)" src="https://github.com/user-attachments/assets/f6e0ac80-c8f1-4eff-8c5f-ddf417aac565" />
<img width="100%" alt="알림 목록(카테고리별)" src="https://github.com/user-attachments/assets/e29efa8a-2055-41b1-bb56-bb364df5fb21" />
</details>

<details>
<summary><strong>실시간 알림</strong></summary>
<br>
<p><strong>- 멤버 참여 알림</strong></p>
<img width="100%" alt="실시간 알림(멤버 참여)" src="https://github.com/user-attachments/assets/b2a4e112-956f-4a19-8300-1307937a088c" />
<p><strong>- 유통기한 임박 알림</strong></p>
<img width="100%" alt="실시간 알림(유통기한)" src="https://github.com/user-attachments/assets/5251f06e-6a57-4ffa-bb63-01b9c0c9d50f" />
</details>

</details>
