# 스마트 냉장고: 공용 냉장고 관리 웹 서비스

공용 냉장고의 청결과 사용자 간의 소통을 돕는 음식 등록 및 관리 웹 서비스입니다.

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

### **주제 선정 배경**
* **공동 냉장고 사용 시 식자재 관리의 어려움**
    * 여러 사람이 함께 사용하는 냉장고에서는 누가 어떤 식재료를 넣었는지, 유통기한이 언제인지 파악하기 어렵습니다.
    * 유통기한이 지난 음식물이 방치되거나, 중복 구입으로 인한 자원 낭비가 빈번히 발생합니다.

* **책임 소재 불분명으로 인한 불편**
    * 음식이 상하거나 냄새가 날 경우, 누구의 물건인지 몰라 처리하지 못하고 갈등이 생깁니다.
    * 공동 냉장고의 청결 및 관리 문제로 생활의 질이 저하됩니다.

* **기록 부재로 인한 비효율**
    * 메모지나 마커 등 수기로 남기는 방식은 지속되기 어렵고, 정보 공유가 제한적입니다.
    * 디지털 시스템을 통해 실시간 등록, 조회, 유통기한 알림이 가능한 관리 시스템이 필요합니다.

* **소규모 공동체 내 자원 순환 및 협력 문화 조성**
    * 남은 재료를 등록하고 필요한 사람이 활용하는 환경을 조성하여 식자재 낭비를 줄이고 상호 협력을 강화합니다.

### **기대 효과**
* **냉장고 관리 효율성 향상**
    * 식자재의 등록, 조회, 삭제를 웹에서 간편하게 처리하여 유통기한, 수량 등을 한눈에 확인할 수 있습니다.
    * 불필요한 중복 구매 및 음식물 방치를 방지하고, 관리자의 공지사항 기능으로 사용 규칙을 쉽게 전파할 수 있습니다.

* **유통기한 기반 자동 알림으로 낭비 감소**
    * 유통기한 임박 시 알림 기능을 통해 음식물 폐기를 줄이고 자원의 낭비를 최소화합니다.

* **책임소재 명확화로 갈등 감소**
    * 사용자별로 등록된 식자재 정보를 관리하여, '이건 누구 음식?'과 같은 문제를 해결하고 불필요한 갈등을 예방합니다.

* **자원 순환 및 공유 문화 확산**
    * 사용하지 않는 식자재를 공유함으로써 자원 재사용을 촉진하고, ‘냉털(냉장고 털기)’ 문화 정착에 기여합니다.

* **사용자 간 채팅 기능으로 소통 강화**
    * 식자재 공유 여부, 사용 문의 등을 위한 채팅 기능으로 협업 및 관리 효율을 증대합니다.
    * 예: "이 우유 써도 될까요?", "오늘까지 먹어야 하는 김밥 있어요" 등의 대화로 소통을 원활하게 합니다.

---

## 🛠️ 기술 스택

### **데이터베이스**
![MariaDB](https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Amazon S3](https://img.shields.io/badge/amazons3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)

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

## 📋 프로젝트 산출물

| 구분 | 링크 |
| :--- | :--- |
| **Figma** | [🔗 Figma 디자인 보기](https://www.figma.com/design/wU7s6yvtkBz0H1dKfz3wwH/SmartFridge?node-id=95-96&p=f&t=si8Msm4lkbOdoM8i-0) |
| **API 명세서** | [🔗 API 명세서 보기](https://docs.google.com/spreadsheets/d/1nH7eOviqjvMAsm5lpgn3xR_aUZIRZFVX/edit?gid=2135429763#gid=2135429763) |
| **WBS** | [🔗 WBS 보기](https://docs.google.com/spreadsheets/d/1MdMci2yeFGn26Q4miN4VBFLd0V3hfnp7/edit?gid=1981675247#gid=1981675247) |
| **요구사항 명세서** | [🔗 요구사항 명세서 보기](https://docs.google.com/spreadsheets/d/1VasDKfhetE3DzFZJidi0ld-xMtJ5dCzDggyUzre7fkc/edit?gid=0#gid=0) |

<br/>

## 💾 ERD

<img width="100%" alt="SmartFridge ERD" src="https://github.com/user-attachments/assets/8ccf37fb-154c-4865-b3f2-44fc2f67ba57" />
