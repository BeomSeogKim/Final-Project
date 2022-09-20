# 프로젝트 명 : 삼삼오오(3355) 
### 원하는 활동이 비슷한 사람들 끼리 모여서 소통하고 모임 계획을 잡을 수 있는 플랫폼

#### Front-End GitHub : https://github.com/hyerriimm/Final_Project

# 주요 기능 
1. 회원가입 및 로그인
   - 카카오톡을 이용한 소셜 로그인
   - JWT 발급을 통한 로그인





# Trouble Shooting 
1. Error CODE : File 'id' doesn't have a default value 
    * 원인 : 처음에 데이터 베이스를 생성 시 **@GeneratedValue(strategy = IDENTITY)** 를 사용하지 않고 데이터를 생성하여 primary 키에 AI(Auto Increase) 조건이 붙지 않음 그에 반해 코드에서는 @GeneratedValue(strategy=IDENTITY)를 사용 하여 에러 발생
    * 해결방법 : 새로 데이터 베이스를 구축하여 에러 해결  
2. 






#ERD
![Final](https://user-images.githubusercontent.com/110332047/191170631-651a5269-3f33-4899-9333-559441cc93ac.png)

# API 명세서 
/==/


