# :pushpin: 삼삼오오(3355)
>같은 관심사를 가진 사람들을 모으고 소통할 수 있도록 돕는 소모임 커뮤니티  
>https://3355.world  

</br>

## 1. 제작 기간 & 참여 인원
- 2022년 9월 16일 ~ 10월 28일
- 팀 프로젝트
  - [FrontEnd] : 김고은(부 팀장), 이경하, 이혜림
  - [BackEnd]  : 김범석(팀장), 김하영 

</br>

## 2. 사용 기술
#### `Backend`
  - Java 11
  - Spring Boot 2.7.3
  - Gradle 7.5
  - Spring Data JPA
  - QueryDSL 1.0.10
  - H2 1.4.200
  - MySQL 8.0.28
  - Spring Security
  - Socket.IO
  - Junit5
  - Jmeter

</br>

## 3. ERD 설계
![image](https://user-images.githubusercontent.com/110332047/197563865-388aeeb3-2854-41c0-842b-200f174903c8.png)


## 4. 핵심 기능
이 서비스의 핵심 기능은 컨텐츠 등록 및 채팅 기능.   
사용자들은 모임을 갖고 싶은 카테고리로 모임을 개설 가능.   
각 사용자들은 원하는 모임에 참여하여 서로 얘기를 나누며 오프라인 만남도 가질 수 있도록 장려.

<details>
<summary><b>핵심 기능 설명 펼치기</b></summary>
<div markdown="1">

### 4.1. 회원가입 및 로그인 
  - 카카오톡을 이용한 소셜 로그인 
  - JWT 발급을 통한 로그인 
  - 토큰 재발급 구현
  - 회원 정보 수정 구현
  - 회원 탈퇴 구현

### 4.2. 게시글 
  - 게시글 업로드 구현
  - 카테고리별 다른 기본 이미지 업로드
  - 스케쥴러 도입
    1. 모집 마감일이 지났을 때 모집인원이 0인 경우 CLOSE로 업데이트
    2. 모집 마감일이 지났을 때 모집인원이 1명 이상인 경우 DONE으로 업데이트 

### 4.3. 지원 신청 기능 
  - 모임 주최가가 아닌 회원의 경우 모임 참가신청 및 취소 가능
  - 모임 주최자가 참가 신청 수락 시 모임 참여 가능

### 4.4 채팅 기능
  - 게시글 작성시 채팅방 생성
  - 다른 지원자들의 참가 신청이 수락될 경우 채팅방에 참여 
  - 채팅방에 읽지 않은 채팅 메세지 개수 조회 가능
  - 
### 4.5. 실시간 알림 기능 
  - 회원이 작성한 게시글에 댓글이 달린 경우 실시간 알림 전송
  - 회원이 작성한 게시글에 신청이 있을 경우 실시간 알림 전송
  - 모임 신청 거절 시 실시간 알림 전송
  - 모임 신청 승인 시 실시간 알림 전송 

### 4.5. 신고 기능 
  - 게시글, 댓글 및 회원에 대해 신고 가능 
  - 누적 신고 처리 횟수 10회 이상이 되면 회원 제재 
  - 관리자만 관리할 수 있도록 관리자 권한부여

### 4.6. Nginx 도입 



</div>
</details>

</br>

## 5. 기능 개선 사항 
<details>
<summary> 상세보기 </summary>
<div markdown="1">

### 5.1. 회원 가입 
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전 
  - 카카오로 로그인 하는 유저와 일반 가입 유저를 구분하지 않고 DataBase에 저장
- 개선 후 
  - 카카오를 사용하여 로그인 하는 유저는 kakao, 일반 회원 가입을 하는 유저는 normal로 변경
- 개선 이유 
  - 추후에 카카오 유저와 일반 유저와 관련된 추가 기능 개발의 여부를 고려하여 개선을 진행

</div>
</details>

### 5.2. 회원 수정 
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전
  - 회원 정보 수정 시 성공적으로 회원 정보 수정이 되었음만을 알려줌 
- 개선 후  
  - 회원 정보 수정시 response Header에 재발급 된 토큰 반환 
- 개선 이유 
  - 회원 정보 수정시 기존 회원이 가지고 있는 토큰이 만료되어 강제 로그아웃이 됨 
  - 이를 방지하기 지속적으로 로그인 상태를 유지할 수 잇도록 토큰을 header에 발급 

</div>
</details>


### 5.3. 게시글 상태 변경 (날짜 기준)
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전  
  - 모임의 상태를 관리자가 수동적으로 조작을 해주어야 함 
- 개선 후 (1차)  
  - 스케쥴러 도입 후 매일 새벽 1시에 모집 마감일이나 모임일이 현재 기준으로 지난 경우 DONE 혹은 CLOSURE 상태로 변경 
- 개선 이유 
  - 관리자가 매일 일일히 게시글 상태를 모니터링하며 상태를 바꿔 주는 것은 비효율적인 로직이라 판단되어 자동화로 변경함
- 개선 후 (2차)
  - 스케쥴러가 작동할 때 만약 모집 인원이 0명인 게시글의 경우 CLOSURE 으로 상태 변경 
  - 스케쥴러가 작동할 때 만약 모집 인원이 1명이라도 있는 경우 DONE 으로 상태 변경 
- 개선 이유 
  - 게시글 모집 마감 혹은 모집 종료(비활성화)를 구분하기 위해 상세 로직 추가 

</div>
</details>

### 5.4. 게시글 상태 변경 (모집 인원 기준)
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전  
  - 해당 모임에 신청 정원이 다 찼을 때에도 모임의 상태가 DONE으로 변경되지 않음
- 개선 후  
  - 해당 모임에 신청 정원이 다 차게 되면 DONE 상태로 변경
- 개선 이유 
  - 관리자가 일일히 모집 상태를 바꾸는 것은 비효율적이라 판단됨
  - 신청정원이 다 찼는데도 신청 승인을 할 수 있는 문제가 생김

</div>
</details>

### 5.5. Pagination
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전 
  - Page 정보 관련한 내용들을 일일히 로직 구현
- 개선 후 
  - JPA 에서 제공하는 Page< > 를 사용하여 페이지 관련 정보들을 추출
- 개선 이유 
  - Page 관련한 정보들을 직접 구현하는데 있어 잦은 실수 및 에러가 발생할 가능성이 높아보여 안전한 방법으로 구현

</div>
</details>

### 5.5. 동적 검색
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전 
  - 동적 검색 기능구현을 할 때 keyword 및 category 의 존재 유무에 따라 조건문을 사용하여 검색 기능을 구현
- 개선 후 
  - Querydsl 을 사용하여 유연한 검색 기능을 구현
- 개선 이유 
  - null 값 혹은 Empty 값 들어오는 조건에 대해 필터링을 하는 것에 있어 로직이 많이 복잡해지는 것을 확인
  - 로직의 실수를 줄이기 위해 Querydsl 적용

</div>
</details>

### 5.6. 관리자 권한 수정 
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전
    - 기존 관리자 권한의 경우 댓글 및 게시글에 대해 신고가 들어왔을 때에만 삭제가 가능
_- 개선 후
    - 관리자 권한을 가지고 있을 경우 신고가 들어오지 않아도 직접 게시글 및 댓글 삭제가 가능 
- 개선 이유
    - 관리자가 더욱 효율적으로 게시글 및 댓글 관리를 할 수 있도록 개선을 진행

</div>
</details>
  
### 5.7. 토큰 전략 변경 
<details>
<summary> 상세보기 </summary>
<div markdown="1">

- 개선 전
    - 전체 접근이 가능한 일부 API를 제외한 나머지 API의 경우 AccessToken과 RefreshToken을 사용하여 회원 검증을 진행
_- 개선 후
    - 토큰 재발급을 제외한 나머지 API에서는 AccessToken만 사용 
- 개선 이유
    - 모든 API에서 RefreshToken을 보낼 경우 탈취되면 큰 문제가 발생한다 판단하여 토큰 전략 변경 진행

</div>
</details>


</div>
</details>




</br>

## 6. 핵심 트러블 슈팅
### 6.1. 회원 탈퇴 이슈 
<details>
<summary><b>상세 내용 조회</b></summary>
<div markdown="1">

  - 도입 이유 
    - 회원이 탈퇴할 때 회원의 정보를 따로 처리하기 위해 도입 
  - 문제 발생 
    - 회원이 탈퇴할 경우 탈퇴한 회원의 관련된 활동들이 삭제되어야 하는 상황이 발생. 이로 인하여 다른 사람들의 활동 지표가 떨어지는 현상 발생 
  - 문제 원인 
    - Member와 Post 및 다른 Entity들이 연관관계가 맺어져 있어 회원을 삭제해야 하는 상황이 발생
  - 선택지 
    1) 각 데이터들의 연관 관계를 끊어서 서비스를 운영
    2) 회원 정보를 영구적으로 바꾸어 서비스를 운영 
    3) 회원 탈퇴 관련 Table을 하나 더 생성하여 서비스를 관리 
  - 의견 결정 
    - 유연한 객체 지향 프로그래밍을 위해서는 연관관계를 맺는 것이 좋다고 판단.
    - 회원 탈퇴시 재가입의 경우를 고려해 필요한 정보들을 일정기간 보관하는 것이 좋다고 판단.
    - 회원 탈퇴시 중요한 일부 정보를 다른 Table에 보관하고 Member Table에는 중요한 정보를 삭제
    - 탈퇴 후 5년이 지난 회원의 경우 Scheduler를 사용하여 자동적으로 회원 정보가 삭제되는 것으로 문제 해결
</div>
</details>

### 6.2 HikariPool 관련 이슈 
<details>
<summary><b>상세 내용 조회</b></summary>
<div markdown="1">

  - 문제 발생 
    - 실시간 알림을 구현한 후로 서버에 배포하였을 때 배포한 지 얼마 지나지 않아 서버 전반적으로 데이터들이 조회가 안되는 현상 발생
  - 문제 원인 
    - Client가 구독을 할 때 마다 Hikari Connection Pool( 이하 CP)을 사용하고 반납을 하지 않음
    - 이로 인해 다른 요청들이 CP를 받지 못하고 기다리다가 시간 초과로 인해 문제 발생 
  - 해결 방안 
    1) 1차 해결 방안
       - application.properties에서
       - > spring.datasource.hikari.maximum-pool-size = 30 으로 CP를 늘려줌  
    2) 2차 해결 방안 
       - Spring에서 Open-Session-In-View (이하 OSIV) : true 설정이 기본값
       - OSIV가 true 이면 영속성 컨텍스트가 살아있는 주기가 길다는 문제가 존재 
       - 이를 해결하기 위해 OSIV :off 설정하여 트랜잭션 종료시 영속성 컨텍스트가 닫힐 수 있도록 해결 

</div>
</details>

### 6.3 No Session 관련 이슈
<details>
<summary><b>상세 내용 조회</b></summary>
<div markdown="1">

- 문제 발생
    - post 관하여 equals 문법을 사용하는 구문에서 No Session Error가 발생
- 문제 원인
    - Entity 연관관계 설정에서 Lazy Loading이 설정이 되어 있어 No Session 문제가 발생
    - LazyLoading이 되어 있을경우 해당 Entity 값을 직접꺼내지 않는다면 Proxy 객체를 반환
    - Proxy 객체와 객체를 비교할 수 없어 에러가 발생 
- 해결 방안
    - 같은 세션을 유지하기 위해 @Transactional 어노테이션을 사용하여 해결을 진행함.
</div>
</details>

### 6.4. Nginx 관련 설정 이슈 
<details>
<summary><b>상세 내용 조회</b></summary>
<div markdown="1">

- 도입 이유 
    - 기존에는 Nginx를 적용하지 않고 서버를 운영하고 있었으나, 무중단 배포 및 https 적용 및 비용의 효율성으로 인해 도입을 하게 됨
- 문제 발생 
    - 무중단 배포, 안정적인 서버 및 https를 용이하게 사용하기 위해 서버에 Nginx를 적용한 뒤로
    - 그동안 잘 작동하던 WebSocket 및 ServerSentEvent가 동작하지 않는 현상이 발생
      (failed: Error during WebSocket handshake: Unexpected response code:200)
  - 웹소켓 
    - 문제 원인
      - 웹소켓의 경우 response로 status code 101 (switching protocol)를 반환해야 하지만 200 OK 가 전달되어 나타나는 오류
    - 선택지 
      1) 웹소켓을 선택하기 전으로 서버를 되돌리기
      2) Nginx 추가 설정을 하여 통신을 할 수 있도록 해결하기
    - 의견 결정
      - Nginx를 적용한 후로 안정적인 서버 운영이 가능해졌음
      - Nginx를 적용한 후로 비교적 쉽게 https 적용이 가능 
      - Nginx로 인한 이점이 더 많아 nginx.conf 설정을 변경하여 사용할 수 있도록 결정
 
  - SSE(Server-Sent-Event)
      - 문제 원인 
        - Nginx는 기본적으로 Upstream으로 요청을 보낼 때 Http/1.0 버전을 사용
        - Http/1.1의 경우 지속 연결이 기본이기에 헤더를 따로 설정할 필요가 없지만
        - Nginx에서 백엔드의 WAS로 요청을 보낼 때에는 HTTP/1.0을 사용하고 Connection:close 헤더를 사용함.
        - 이로 인해 지속 연결을 계속 닫아 SSE 가 정상적으로 작동하지 않음
      - 선택지 
        1) 웹소켓을 선택하기 전으로 서버를 되돌리기
        2) Nginx 추가 설정을 하여 SSE를 사용
      - 의견 결정 
           - Nginx 를 적용한 후로 안정적인 서버 운영이 가능해졌음
           - Nginx를 적용한 후로 비교적 쉽게 https 적용이 가능
           - Nginx로 인한 이점이 더 많아 nginx.conf 설정을 변경하여 사용할 수 있도록 결정


</div>
</details>

### 6.5 회원 신고 관련 이슈 
<details>
<summary><b>상세 내용 조회</b></summary>
<div markdown="1">

- 도입 이유
    - 커뮤니티 플랫폼 특성상 광고성 게시글 광고성 댓글에 취약함
    - 커뮤니티 플랫폼 특성상 부적절한 댓글 및 게시글을 작성할 가능성이 있음
    - 이에 따라 신고기능이 필요하여 도입
    - **신고의 경우 댓글, 게시글, 회원에 대해 신고 가능** 
- 문제 발생
    - 게시글 및 댓글에 대해서는 정상적으로 게시글을 제재할 수 있음
    - 회원을 신고 처리 시 바로 활동을 못하도록 제재를 해야 하는 상황 발생  
- 문제 원인
    - 신고 접수 처리시 상태 변경하는 로직만이 존재
    - 회원 관련하여 누적 신고가 몇 회 인지 확인 할 방법이 없음 
- 선택지
    1) 회원 신고 처리시 회원을 바로 제재함
    2) 회원 관련하여 신고를 하지 않음 
    3) 누적 신고를 적용하여 누적 신고 횟수가 일정 숫자를 넘을 경우 제재를 가함 
- 의견 결정
    - 신고 처리시 회원을 바로 제재하는 것은 너무 강압적인 방법
    - 채팅 및 다양한 부분에서 악성 회원을 신고할 수 있는 방법이 없음
    - 게시글, 댓글 및 회원 신고에 대해 누적 신고제를 도입
    - 10회 제재를 당하게 되면 회원 활동이 금지됨 
</div>
</details>





</br>

## 7. 그 외 트러블 슈팅
<details>
<summary> Error CODE : File 'id' doesn't have a default value </summary>
<div markdown="1">

- 원인 
  - 처음에 데이터 베이스를 생성 시 id 전략에 **@GeneratedValue(strategy = IDENTITY)** 를 사용하지 않고 데이터를 생성하여 primary 키에 AI(Auto Increase) 조건이 붙지 않음
  - 그에 반해 수정된 코드에서는 @GeneratedValue(strategy=IDENTITY)를 사용 하여 에러 발생
- 해결방법 
  - MySQL에서 primary key 에 AI 조건 설정 

</div>
</details>

<details>
<summary> LocalDate.parse 가 제대로 동작하지 않는 현상   </summary>
<div markdown="1">

- 원인
    - 기존에 LocalDateTime 으로 작업을 하던 상황이기에 Method 들이 LocalDateTime 으로 구현되어 있었음
- 해결방법 
  - Entity 및 Dto LocalDateTime 및 LocalDateTime 형식을 LocalDate 로 변경

</div>

</details>

<details>
<summary> 무한 로그아웃 현상 </summary>
<div markdown="1">

- 원인
    - 로그아웃시 Database에서 RefreshToken 이 유효한지 검증하는 로직이 없었음
    - 이로 인해 기존에 들고 있던 RefreshToken 으로 무한 로그아웃이 가능한 현상이 발생 
- 해결방법 
  - logout시 우선적으로 Database에 RefreshToken 이 존재하는지 검사하는 로직 추가  

</div>
</details>

<details>
<summary> Nginx 관련 서버 세팅 에러 </summary>
<div markdown="1">

- 1차 문제  
  - 원인
    - server 설정과 관련된 nginx.conf 파일의 잘못된 위치에 코드 작성
  - 해결방법 
    - mail 문단에 작성되어있던 server 정보 파일을 http 문단으로 이전
- 2차 문제 
  - 원인 
    - sh파일에서 주석처리 관련 잘못 기입된 부분이 있어 sh가 정상적으로 작동되지 않음
  - 해결방법 
    - 문제가 발생하던 주석을 찾아 수정
- 3차 문제
  - 원인 
    - 검증 로직 관련하여 10번의 iteration 진행 시 error가 발생
  - 해결방법 
    - 10번의 iteration을 5번의 iteration으로 줄인 후 정상적으로 작동하는 것을 확인
- 최종 해결방안 
  - 기존의 경우 EC2를 ubuntu를 사용하고 있었음
  - EC2에는 기본적으로 nginx.conf 파일이 잘 되어있지 않았음
  - 이에 따라 EC2 를 Amazon Linux를 사용함

</div>
</details>





    
</br>

## 8. 회고 / 느낀점
>프로젝트 개발 회고 글: 추후 작성 예정.
