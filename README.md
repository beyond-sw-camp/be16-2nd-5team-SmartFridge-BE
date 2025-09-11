# 스마트 냉장고: 공용 냉장고 관리 웹 서비스

<div align="left">
  <img src="https://cdn.discordapp.com/attachments/1386519346293112972/1409777259983667230/image.png?ex=68ae9ce3&is=68ad4b63&hm=dd69c7da78d11a8ad7f81a7af192bb618700e1b8ee175619885ee15d05ba4b8c&" alt="Smart Fridge Project Banner" width="800"/>
</div>

<br/>

## 🙋🏻 팀원 소개

| **김영관** | **김지현** | **윤세진** | **김찬진** |
| :---: | :---: | :---: | :---: |
| [<img src="https://avatars.githubusercontent.com/u/154659797?v=4" height=150 width=150> <br/> @YoungKwanK](https://github.com/YoungKwanK) | [<img src="https://avatars.githubusercontent.com/u/80299604?v=4" height=150 width=150> <br/> @Jihyeon0804](https://github.com/Jihyeon0804) | [<img src="https://avatars.githubusercontent.com/u/208623826?v=4" height=150 width=150> <br/> @AstroJini](https://github.com/AstroJini) | [<img src="https://avatars.githubusercontent.com/u/113010788?v=4" height=150 width=150> <br/> @Chanjin629](https://github.com/Chanjin629) |

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

### **💻 Tech Stacks**

#### **Backend**
![Java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring_boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![JWT](https://img.shields.io/badge/jwt-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)

#### **Frontend**
![Vue.js](https://img.shields.io/badge/vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Vuetify](https://img.shields.io/badge/vuetify-1867C0?style=for-the-badge&logo=vuetify&logoColor=white)
![Vuex](https://img.shields.io/badge/vuex-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Vue Router](https://img.shields.io/badge/vue_router-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Axios](https://img.shields.io/badge/axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white)
![JavaScript](https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![CSS3](https://img.shields.io/badge/css3-1572B6?style=for-the-badge&logo=css3&logoColor=white)

#### **Database & Storage**
![MariaDB](https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Amazon S3](https://img.shields.io/badge/amazon_s3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white)

#### **Real-time Communication**
![WebSocket](https://img.shields.io/badge/WebSocket-019533?style=for-the-badge&logo=websocket&logoColor=white)
![StompJS](https://img.shields.io/badge/StompJS-F05032?style=for-the-badge)
![RabbitMQ](https://img.shields.io/badge/rabbitmq-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![SSE](https://img.shields.io/badge/SSE-000000?style=for-the-badge)

#### **DevOps & Infrastructure**
![Amazon EC2](https://img.shields.io/badge/amazon_ec2-FF9900?style=for-the-badge&logo=amazon-ec2&logoColor=white)
![Amazon EKS](https://img.shields.io/badge/amazon_eks-FF9900?style=for-the-badge&logo=amazon-eks&logoColor=white)
![Docker](https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github_actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

#### **Tools & Collaboration**
![Gradle](https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Git](https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Figma](https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Discord](https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
![ERDCloud](https://img.shields.io/badge/ERDCloud-1E90FF?style=for-the-badge)

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

<br/>

---

## 🔗 시스템 아키텍쳐
<img width="817" height="722" alt="시스템아키텍쳐" src="https://github.com/user-attachments/assets/37b60786-0708-40b4-b481-06656dfbd169" />

<br/>

---

## 테스트 결과물

<details>
<summary><strong>👤 회원 관리</strong></summary>
<br>

<details>
<summary><strong>회원가입 (이메일 인증)</strong></summary>
<br>
<p><strong>- 일반 회원가입 이메일 인증 및 로그인</strong></p>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/c92b42e4-1e46-45fa-830f-04cd8b02af5d" />
</details>

<details>
<summary><strong>소셜 로그인 (Google, Kakao, Naver)</strong></summary>
<br>
<p><strong>- Google</strong></p>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/90cd9b44-f898-4955-acb3-7eb48010d5e5" />
<p><strong>- Kakao</strong></p>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/684a3f9b-2d47-4817-ac50-e948383af451" />
<p><strong>- Naver</strong></p>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/167b62d6-468d-4a33-9552-0c8f0895ead2" />
</details>

<details>
<summary><strong>마이페이지</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/068477c8-a683-476f-adf9-d8525d8c1879" />
</details>

<details>
<summary><strong>회원 탈퇴</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/a669cda2-4560-4b60-a32e-feae6d51456b" />
</details>

<details>
<summary><strong>관리자 승격</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/68f1faa9-be8d-4929-afb9-54eddd19ecbd" />
</details>

<details>
<summary><strong>ADMIN의 회원 삭제</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/c09a50c3-0960-465b-b366-d02da84131c3" />
</details>

</details>

<br>

<details>
<summary><strong>🧊 냉장고 관리</strong></summary>
<br>

<details>
<summary><strong>냉장고 생성</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/05f82e75-dd3d-49bf-879a-fe8d5f4c2843" />
</details>

<details>
<summary><strong>냉장고 들어가기</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/5a9e24f6-de23-456f-bcd8-94a18183293e" />
</details>

<details>
<summary><strong>냉장고 수정</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/e30db0a5-5dbe-4ef9-be05-4eedf029ebc7" />
</details>

<details>
<summary><strong>냉장고 삭제</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/19da3cfc-8880-47b1-b106-07a5a683e43b" />
</details>

<details>
<summary><strong>냉장고 참여</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/64f31049-f3db-49c0-a3d9-301aa21470ef" />
</details>

<details>
<summary><strong>냉장고 나가기</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/987189f1-30d2-4d20-ac6c-0ed5516b5838" />
</details>

<details>
<summary><strong>대시보드</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/21947cf0-3725-4be2-9c66-f3317524f385" />
</details>

</details>

<br>

<details>
<summary><strong>🥕 식품 관리</strong></summary>
<br>

<details>
<summary><strong>식품 등록</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/d5b8e011-b9e6-4079-b0e5-2c2547b3af9b" />
</details>

<details>
<summary><strong>공유 식품 등록</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/fe1b5d7d-7471-4fc3-91cd-90a1ed8cff15" />
</details>

<details>
<summary><strong>식품 임시보관</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/ed3a62c5-c76d-4488-88d9-4600c74384ed" />
</details>

<details>
<summary><strong>식품 수정 및 삭제</strong></summary>
<br><br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/15086660-0d77-4f1f-99a2-09db64255131" />
</details>

</details>

<br>

<details>
<summary><strong>✍️ 게시판</strong></summary>
<br>
<details>
<summary><strong>게시글 등록</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/93e1926a-f8b9-458d-bb8f-abe6f34c4d5f" />
</details>
  
<details>
<summary><strong>게시글 좋아요 및 댓글</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/eafff57c-b26f-4f3f-83f2-5ab7c484b764" />
</details>
</details>

<br>

<details>
<summary><strong>❓ 문의</strong></summary>
<br>

<details>
<summary><strong>문의 등록 및 admin문의 답변</strong></summary>
<br><img width="100%" alt="Image" src="https://github.com/user-attachments/assets/1858e722-0ea8-46b2-8385-2a300c84bda5" />
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
<summary><strong>유통기한알림</strong></summary>
<br>
<p><strong>- 유통기한 임박 알림</strong></p>
<img width="1898" height="911" alt="유통기한 알림 목록" src="https://github.com/user-attachments/assets/e9f98c87-0ea0-4179-86da-bab4ac8bc9ee" />
<img width="1896" height="902" alt="유통기한 알림 배지" src="https://github.com/user-attachments/assets/d2f16b85-c3e9-48ac-9cf6-45bc235fdabe" />
</details>
<details>
<summary><strong>admin 알림 처리</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/e62de591-c3c5-4b72-bcaf-aa61f13ad8e0" />

</details>
<details>
<summary><strong>공동구매채팅방 알림</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/66895cc8-c99d-4c41-a747-203df2c0d83c" />
</details>

<details>
<summary><strong>식품 등록 알림</strong></summary>
<br><img width="100%" alt="Image" src="https://github.com/user-attachments/assets/bedec70f-32c7-45d6-9f09-8dd8f991dbb1" />
<summary><strong>식품 실시간 알림</strong></summary>
<br><img width="100%" alt="Image" src="https://github.com/user-attachments/assets/852a2c75-7198-4026-91b8-afe5cb158080" />

</details>

<details>
<summary><strong>냉장고 멤버 참여 알림</strong></summary>
<br>
<img width="100%" alt="Image" src="https://github.com/user-attachments/assets/9ba7f300-d13d-4c4e-855e-9e309967c676" />
</details>
</details>

<br>

<details>
<summary><strong>💬 채팅</strong></summary>
<br>
<details>
<summary><strong>채팅방 생성</strong></summary>
<br>
</details>
<details>
<summary><strong>채팅방 참여 및 메시지 전송</strong></summary>
<br>
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
</details>
</details>
<br>

---

## 🪄 회고록

| 팀원 | 회고 내용 |
| :--- | :--- |
| **김영관** | 프로젝트 기획 단계에서 ERD와 Figma를 꼼꼼하게 설계한 덕분에, 이후 Entity 설계 및 화면 개발을 매우 수월하게 진행할 수 있었습니다. 기획을 탄탄하게 다지는 과정이 실제 개발 효율에 얼마나 큰 영향을 미치는지 체감할 수 있었습니다.<br><br>개발 중에는 예외 처리가 가장 큰 고민이었지만, 공통 Validator를 만들어 적용함으로써 복잡성을 줄이고 효율적으로 해결했습니다. 또한, 상위-하위 도메인을 동시에 개발하며 의존성 문제로 어려움을 겪었는데, 이를 통해 상위 도메인을 먼저 개발하고 공통 모듈을 미리 구현하는 것의 중요성을 배우게 되었습니다.<br><br>기술적으로는 실시간 채팅 구현을 위해 WebSocket 기술을 도입하여 서버와 클라이언트 간의 효율적인 양방향 통신 아키처를 설계했습니다.<br>특히 GitHub Actions를 활용해 CI/CD 파이프라인을 구축하여, 코드가 특정 브랜치에 푸시될 때마다 자동으로 테스트, 빌드, 그리고 컨테이너 이미지 생성 후 레지스트리에 푸시하는 과정을 자동화했습니다. 최종적으로 이렇게 완성된 이미지를 Kubernetes(EKS) 환경에 무중단으로 배포하도록 구성하며, 전체 개발 플로우의 생산성을 높이고 DevOps 역량을 기를 수 있었던 값진 경험이었습니다. |
| **김지현** | 사용자에게 실시간 알림을 효율적으로 전달하는 시스템을 구축했습니다. 초기에는 양방향 통신이 가능한 WebSocket을 고려했으나, 알림 서비스의 주된 목적이 서버에서 클라이언트로의 단방향 메시지 전송이라는 점에 주목하여 SSE(Server-Sent Events)를 선택했습니다.<br/><br/>이는 불필요한 연결 오버헤드를 줄이고 서버 리소스를 효율적으로 사용하면서도 안정적인 실시간 알림을 구현하는 데 큰 도움이 되었습니다.<br/>사용자로부터 '새로운 식품 등록', '댓글 작성' 등 이벤트가 발생하면, 알림 객체 생성 후 DB에 모든 알림 정보를 저장함으로써,<br/>사용자는 언제든지 알림 히스토리를 확인할 수있도록 하였습니다.<br/><br/>알림 설정이 활성화된 사용자에게 즉시 알림을 보내기 위해 Redis Pub/Sub 을 사용하였습니다.<br/>알림 전송 전에 사용자별 알림 설정을 확인하여, 사용자가 특정 알림 유형을 비활성화한 경우 메시지를 보내지 않도록 구현했습니다.<br/>사용자에게 불필요한 알림을 주지 않고, 개인화된 경험을 제공할 수 있도록 하였습니다.<br/>유통기한 알림은 스케쥴러를 활용하여 특정 시간에 자동으로 사용자에게 발송되도록 하였습니다.<br/><br/>현재는 클라이언트의 수신 성공 여부를 확인하는 피드백 루프가 없어 메시지 유실 가능성이 존재합니다. 이 문제를 해결하기 위해 향후 메시지 큐를 도입하여 안정적인 메시지 재전송 시스템을 구축할 계획입니다. |
| **김찬진** | 이번 프로젝트를 통해 Redis, RabbitMQ등 새로운 기술을 직접 적용하며 개발 역량을 넓힐 수 있었습니다. 단순히 기능을 구현하는 데 그치지 않고, 문제를 분석하고 해결하는 과정에서 많은 배움을 얻었습니다.<br>팀 단위로 진행하면서 협업의 중요성도 체감했습니다. 각자 맡은 기능을 개발했지만, 이슈를 공유하고 해결하는 과정에서 팀워크가 강화되었고, GitHub 협업은 코드 관리와 작업 효율을 높이는 데 큰 도움이 되었습니다.<br>특히 초반 기획을 충분히 준비한 덕분에 개발 과정이 안정적으로 진행되었습니다. 요구사항을 명확히 정의하고 흐름을 설계한 결과, 큰 수정 없이 기능을 구현할 수 있었고 전체적인 완성도를 높일 수 있었습니다.<br>아쉬운 점은 단위 테스트를 충분히 진행하지 못해 일정에 일부 차질이 생긴 것입니다. 앞으로는 테스트 과정을 강화하여 더욱 안정적인 개발을 하고 싶습니다. |
| **윤세진** | 이번 프로젝트에서 저는 OAuth와 SMTP 관련 핵심 기술을 담당했습니다. 프로젝트 진행 중 다양한 변경사항과 트러블 이슈가 발생했는데, 이를 신속하게 해결하는 데 집중하다 보니 아쉬운 점이 있었습니다. 문제를 해결하는 과정에서의 시행착오와 해결 방법을 문서화하지 못해, 팀원들이 참고할 자료가 부족했다는 점은 다음 프로젝트에서 반드시 개선해야 할 부분이라고 생각합니다.<br/><br/>협업 측면에서는 긍정적인 경험이 많았습니다. 팀원들이 서로의 의견을 존중해주었기 때문에 의사소통이 원활했고, 덕분에 팀 내 분위기도 매우 화목했습니다. 다만 프로젝트 초반 기획 단계에서 조금 더 다양한 아이디어를 제안했다면, 더 많은 기능을 담아낼 수 있었을 것이라는 생각이 듭니다.<br/><br/>전반적으로 이번 프로젝트의 가장 큰 성과는 원활한 의사소통과 진중한 태도로 임한 팀 분위기라고 생각합니다. 이러한 협업 문화 덕분에 높은 완성도의 결과물을 만들어낼 수 있었고, 개인적으로도 기술적인 성장뿐만 아니라 협업의 중요성을 다시 한 번 체감할 수 있었습니다. |

<br/>
