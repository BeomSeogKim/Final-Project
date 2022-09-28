package Backend.FinalProject.service;

import Backend.FinalProject.domain.ImageFile;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.RefreshToken;
import Backend.FinalProject.domain.enums.Authority;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.TokenDto;
import Backend.FinalProject.dto.request.LoginRequestDto;
import Backend.FinalProject.dto.request.MemberEditRequestDto;
import Backend.FinalProject.dto.request.MemberUpdateDto;
import Backend.FinalProject.dto.request.SignupRequestDto;
import Backend.FinalProject.repository.FilesRepository;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.repository.RefreshTokenRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final AmazonS3Service amazonS3Service;
    private final FilesRepository fileRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    String baseImage = "https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/memberImage/6c6c20cf-7490-4d9e-b6f6-73c185a417dd%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.webp";
    String baseAwsImage = "https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/memberImage/6c6c20cf-7490-4d9e-b6f6-73c185a417dd%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.webp";
    String folderName = "/memberImage";



    @Transactional
    public ResponseDto<String> createMember(SignupRequestDto request) {

        String userId = request.getUserId();
        String password = request.getPassword();
        String passwordCheck = request.getPasswordCheck();
        String nickname = request.getNickname();
        MultipartFile imgFile = request.getImgFile();
        String imgUrl;

        // null 값 및 공백이 있는 값 체크하기
        if (userId == null || password == null || nickname == null) {

            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (userId.trim().isEmpty() || password.trim().isEmpty() || nickname.trim().isEmpty()) {
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }
        // 비밀번호 및 비밀번호 확인 일치 검사
        if (!password.equals(passwordCheck))
            return ResponseDto.fail("DOUBLE-CHECK_ERROR", "두 비밀번호가 일치하지 않습니다");
        // 아이디 중복검사
        if (!isPresentId(userId).isSuccess())
            return ResponseDto.fail("ALREADY EXIST-ID", "이미 존재하는 아이디 입니다.");
        // 닉네임 중복검사
        if (!isPresentNickname(nickname).isSuccess())
            return ResponseDto.fail("ALREADY EXIST-NICKNAME", "이미 존재하는 닉네임 입니다.");
        // 이미지를 업로드 하지 않을 시 기본 이미지 설정
        if (imgFile == null || imgFile.isEmpty()) {
            imgUrl = baseImage;
        } else {
            // 이미지 업로드 관련 로직
            ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
            ImageFile imageFile = (ImageFile) image.getData();
            imgUrl = imageFile.getUrl();
        }

        Member member = Member.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .imgUrl(imgUrl)
                .userRole(Authority.ROLE_MEMBER)
                .build();

        memberRepository.save(member);
        return ResponseDto.success(member.getUserId() + "님 회원가입 성공");
    }

    @Transactional
    public ResponseDto<String> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        Member member = isPresentMember(loginRequestDto.getUserId());

        if (member == null) {
            return ResponseDto.fail("INVALID_ID", "존재하지 않는 아이디입니다.");
        }
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            return ResponseDto.fail("INVALID_PASSWORD", "잘못된 비밀번호 입니다.");
        }

        // 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        // 헤더에 토큰 담기
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("ImgUrl", member.getImgUrl());
        response.addHeader("Id", member.getUserId());


        return ResponseDto.success(member.getUserId() + "님 로그인 성공");
    }

    @Transactional
    public ResponseDto<?> updateMember(MemberUpdateDto request, HttpServletRequest httpServletRequest) {
        String imgUrl;

        String userId = request.getNickname();
        String password = request.getPassword();
        String passwordCheck = request.getPasswordCheck();
        MultipartFile imgFile = request.getImgFile();

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validateCheck(httpServletRequest);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        Member findMember = memberRepository.findById(member.getId()).get();

        if (userId == null || password == null || passwordCheck == null) {

            return ResponseDto.fail("NULL_DATA", "입력값을 다시 확인해주세요");
        } else if (userId.trim().isEmpty() || password.trim().isEmpty() || passwordCheck.trim().isEmpty()) {
            return ResponseDto.fail("EMPTY_DATA", "빈칸을 채워주세요");
        }

        if (!password.equals(passwordCheck))
            return ResponseDto.fail("DOUBLE-CHECK_ERROR", "두 비밀번호가 일치하지 않습니다");

        if (request.getNickname() != null)
            findMember.updateNickname(userId);

        if(request.getPassword() != null)
            findMember.updatePassword(passwordEncoder.encode(password));

        if (!imgFile.isEmpty()){
            if (member.getImgUrl().equals(baseImage)) {
                ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
                ImageFile imageFile = (ImageFile) image.getData();
                imgUrl = imageFile.getUrl();
                findMember.updateImage(imgUrl);
            } else {
                ImageFile findImageFile = fileRepository.findByUrl(member.getImgUrl());
                amazonS3Service.removeFile(findImageFile.getImageName(), folderName);
                ResponseDto<?> image = amazonS3Service.uploadFile(imgFile, folderName);
                ImageFile imageFile = (ImageFile) image.getData();
                imgUrl = imageFile.getUrl();
                findMember.updateImage(imgUrl);
            }
        }

       return ResponseDto.success("성공적으로 회원 수정이 완료되었습니다");
    }

    @Transactional
    public ResponseDto<?> logout(HttpServletRequest request) {

        String refreshToken = request.getHeader("RefreshToken");
        RefreshToken validateToken = refreshTokenRepository.findByKeyValue(refreshToken).orElse(null);

        if (validateToken == null) {
            return ResponseDto.fail("ALREADY LOGOUT", "이미 로그아웃 하셨습니다.");
        }

        if(!tokenProvider.validateToken(request.getHeader("RefreshToken")))
            return ResponseDto.fail("INVALID TOKEN","토큰 값이 올바르지 않습니다.");

        // 맴버객체 찾아오기
        Member member = tokenProvider.getMemberFromAuthentication();
        if (null == member)
            return ResponseDto.fail("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
        tokenProvider.deleteRefreshToken(member);


        return ResponseDto.success("로그아웃 완료");
    }

    @Transactional
    public ResponseDto<?> signout(HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        if (!member.getImgUrl().equals(baseAwsImage)) {
            ImageFile deleteImage = fileRepository.findByUrl(member.getImgUrl());
            amazonS3Service.removeFile(deleteImage.getImageName(), folderName);
        }
        refreshTokenRepository.deleteById(member.getUserId());
        memberRepository.delete(member);


        return ResponseDto.success("회원 탈퇴가 성공적으로 수행되었습니다.");
    }



    // 회원 아이디 중복 검사 method
    public ResponseDto<String> isPresentId(String id) {
        Optional<Member> userId = memberRepository.findByUserId(id);
        if (userId.isPresent()) {
            return ResponseDto.fail("ALREADY EXIST-ID", "이미 존재하는 회원 아이디입니다.");
        } else return ResponseDto.success("사용 가능한 아이디입니다.");
    }

    // 닉네임 중복 검사 method
    public ResponseDto<String> isPresentNickname(String nickname) {
        Optional<Member> findNickname = memberRepository.findByNickname(nickname);
        if (findNickname.isPresent()) {
            return ResponseDto.fail("ALREADY EXIST-NICKNAME", "이미 존재하는 닉네임입니다.");
        } else return ResponseDto.success("사용 가능한 닉네임입니다.");
    }

    // 회원 검색
    @Transactional(readOnly = true)
    public Member isPresentMember(String userId) {
        Optional<Member> findMember = memberRepository.findByUserId(userId);
        return findMember.orElse(null);
    }

    // RefreshToken 유효성 검사
    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }

    // T
    private ResponseDto<?> validateCheck(HttpServletRequest request) {

        // RefreshToken 및 Authorization 유효성 검사
        if (request.getHeader("Authorization") == null || request.getHeader("RefreshToken") == null) {
            return ResponseDto.fail("NEED_LOGIN", "로그인이 필요합니다.");
        }
        Member member = validateMember(request);

        // 토큰 유효성 검사
        if (member == null) {
            return ResponseDto.fail("INVALID TOKEN", "Token이 유효하지 않습니다.");
        }
        return ResponseDto.success(member);
    }



}
