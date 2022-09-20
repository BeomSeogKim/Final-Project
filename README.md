# 프로젝트 명 : 삼삼오오(3355) 
### 원하는 활동이 비슷한 사람들 끼리 모여서 소통하고 모임 계획을 잡을 수 있는 플랫폼

#### Front-End GitHub : https://github.com/hyerriimm/Final_Project

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




# Trouble Shooting 
1. Error CODE : File 'id' doesn't have a default value 
    * 원인 : 처음에 데이터 베이스를 생성 시 **@GeneratedValue(strategy = IDENTITY)** 를 사용하지 않고 데이터를 생성하여 primary 키에 AI(Auto Increase) 조건이 붙지 않음 그에 반해 코드에서는 @GeneratedValue(strategy=IDENTITY)를 사용 하여 에러 발생
    * 해결방법 : 새로 데이터 베이스를 구축하여 에러 해결  
2. LocalDate.parse 가 제대로 동작하지 않는 형상 
   * 원인 : 기존에 LocalDateTime 으로 작업을 하던 상황이기에 Method 들이 LocalDateTime 으로 구현되어 있었음
   * 해결방법 : Entity 및 Dto LocalDateTime 및 LocalDateTime 메서드 => LocalDate 로 변경. 
   






# ERD
![Final](https://user-images.githubusercontent.com/110332047/191170631-651a5269-3f33-4899-9333-559441cc93ac.png)

# API 명세서 
/==/


