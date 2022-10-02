# 프로젝트 명 : 삼삼오오(3355) 
### 원하는 활동이 비슷한 사람들 끼리 모여서 소통하고 모임 계획을 잡을 수 있는 플랫폼

#### Front-End GitHub : https://github.com/hyerriimm/Final_Project

## Code Convention
- Naming Convention ⇒ 변수, 함수, 인스턴스를 작성할 때는 Camel Case를 사용.
- 글자의 길이 : 20자 이내로 제한
- 약칭은 되도록이면 사용하지 않기
- else-if 는 되도록이면 사용 지양

## 기술 스택
* SpringBot
* Java
* S3
* EC2
* CodeDeploy
* STOMP
* MYSQL
* OAUTH
* REDIS
* NginX

# 기능 구현 사항  
1. 회원가입 및 로그인
   - 카카오톡을 이용한 소셜 로그인
   - JWT 발급을 통한 로그인

2. 게시글 업로드 시 날짜 기입 
   - String 으로 받아서 LocalDate.parse 메서드로 LocalDate 타입으로 변경
   - **몇일 전**으로 모집 남은 일자 확인 할 수 있도록 구현 
   - 검증 로직 구현 
     1. 모집 시작일, 모집 마감일, 모임 일이 현재 날짜보다 이전일 경우 Error 처리 
     2. 모집 마감일 및 모임 일이 모집 시작일 보다 이전일 경우 Error 처리
     3. 모임 일이 모집 마감 보다 이전일 경우 Error 처리 
     4. 지정된 ("yyyy-MM-dd") 타입이 아닐 경우에 Error 처리
3. 게시글 관련 스케쥴러 도입
   - 매일 자정 (오전 0시) 기준으로 게시글의 상태 업데이트
     1. 모집마감일이 현재 날짜보다 이전인 경우 모집 완료 상태로 업데이트
     2. 모임일이 현재 날짜보다 이전인 경우 모집 마감 상태로 업데이트 
4. NginX 도입
    - 무중단 배포를 위한 NginX 도입 




# Trouble Shooting 
1. Error CODE : File 'id' doesn't have a default value 
    * 원인 : 처음에 데이터 베이스를 생성 시 **@GeneratedValue(strategy = IDENTITY)** 를 사용하지 않고 데이터를 생성하여 primary 키에 AI(Auto Increase) 조건이 붙지 않음 그에 반해 코드에서는 @GeneratedValue(strategy=IDENTITY)를 사용 하여 에러 발생
    * 해결방법 : 새로 데이터 베이스를 구축하여 에러 해결  
2. LocalDate.parse 가 제대로 동작하지 않는 형상 
   * 원인 : 기존에 LocalDateTime 으로 작업을 하던 상황이기에 Method 들이 LocalDateTime 으로 구현되어 있었음
   * 해결방법 : Entity 및 Dto LocalDateTime 및 LocalDateTime 메서드 => LocalDate 로 변경. 
3. 무한 로그아웃 현상 
   * 원인 : 기존에는 Database 에서 RefreshToken 의 유효성을 검사하는 로직이 없었음. 이로 인해 기존에 들고 있던 RefreshToken 으로 무한 로그아웃이 가능함
   * 해결방법 : logout 시 우선적으로 Database 에 RefreshToken 이 존재하는 지 검사하는 로직을 추가함. 
4. NginX 관련 서버 세팅 에러
   1.
      * 원인 : server 설정과 관련된 nginx.conf 파일의 잘못된 위치에 코드 작성 
      * 해결방법 : mail 문단에 작성되어있던 server 정보 파일을 http 문단으로 이전 
   2.  
      * 원인 : sh파일에서 주석처리 관련 잘못 기입된 부분이 있어 sh가 정상적으로 작동되지 않음
      * 해결방법 : 문제가 발생하던 주석을 찾아 수정
   3.
      * 원인 : 검증 로직 관련하여 10번의 iteration 진행 시 error가 발생
      * 해결방법 : 10번의 iteration을 5번의 iteration으로 줄인 후 정상적으로 작동하는 것을 확인


# 기능 개선 사항
1. 회원가입
    * 개선 전  : 카카오로 로그인 하는 유저와 일반 가입 유저를 구분하지 않고 DataBase에 저장
    * 개선 후  : 카카오를 사용하여 로그인 하는 유저는 kakao, 일반 회원 가입을 하는 유저는 normal로 변경 
    * 개선 이유 : 추후에 카카오 유저와 일반 유저와 관련된 추가 기능 개발의 여부를 고려하여 개선을 진행  

2. 회원수정
   * 개선 전  : 회원 정보 수정을 하게 되었을 때 문자열로 성공적으로 회원 정보 수정이 되었음만을 알려줌 
   * 개선 후  : 회원 정보 수정을 하게 되면 새로운 토큰을 발급하여 response Header에 토큰을 발급함
   * 개선 이유 : 기존 회원이 가지고 있는 토큰이 만료되어 강제 로그아웃이 됨. 이를 방지하기 위해 새로운 토큰을 발급하여 header에 넣어주는 방식을 택함

3. 게시글 상태 변경 
   * 개선 전  : 모임의 상태를 관리자가 수동적으로 조절을 해주어야 함
   * 개선 후  : 스케쥴러 도입 후 모집 마감일이나 모임일이 현재 기준으로 지난 경우 DONE 혹은 CLOSURE 상태로 변경 
   * 개선 이유 : 관리자가 매일 일일히 게시글 상태를 모니터링하며 상태를 바꿔 주는 것은 비효율적인 로직이라 판단되어 자동화로 변경함

4. 게시글 상태 변경 
   * 개선 전  : 해당 모임에 신청 정원이 다 찼을 때에도 모임의 상태가 변경되지 않았음. 
   * 개선 후  : 해당 모임에 신청 정원이 다 차게 되면 모집 완료 상태로 변경. 추가적으로 모집인원 중 한명이 빠져나가게 되면 모집중 상태로 변경
   * 개선 이유 : 관리자가 일일히 모집 상태를 바꾸는 것은 비효율적이라 판단되며, 신청정원이 다 찼는데도 신청 승인을 할 수 있는 문제가 생김
   






# ERD
![Final](https://user-images.githubusercontent.com/110332047/191170631-651a5269-3f33-4899-9333-559441cc93ac.png)

# API 명세서 
/==/


