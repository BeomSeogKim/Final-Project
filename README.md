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


# 기능 개선 사항
1. 회원가입
    * 개선 내용 : 

2. 게시글 상태 변경 
   * 개선 내용 : 
       1. 모집 날짜나 모임일이 현재 기준으로 이전일 경우 기존의 경우에는 일일히 모집 상태를 변경해야 했음. 이러한 부분은 관리자가 수동적으로 모집 상태를 변경해야 하므로 비효율적인 로직이라 판단됨.
       2. 이에 따라 스케쥴러를 도입해 자동적으로 상태를 변경할 수 있게끔 로직 변경  
   






# ERD
![Final](https://user-images.githubusercontent.com/110332047/191170631-651a5269-3f33-4899-9333-559441cc93ac.png)

# API 명세서 
/==/


