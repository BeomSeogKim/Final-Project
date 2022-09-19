package Backend.FinalProject.service;

import Backend.FinalProject.domain.ImageFile;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.enums.Authority;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.SignupRequestDto;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import com.amazonaws.services.ec2.model.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final AmazonS3Service amazonS3Service;


    @Transactional
    public ResponseDto<?> createMember(SignupRequestDto requestDto, MultipartFile imgFile) {
        String userId = requestDto.getUserId();
        String password = requestDto.getPassword();
        String passwordCheck = requestDto.getPasswordCheck();
        String nickname = requestDto.getNickname();
        String imgUrl;
        System.out.println("password = " + password);
        System.out.println("passwordCheck = " + passwordCheck);
        // null 값 및 공백이 있는 값 체크하기
        if (userId == null || password == null || nickname == null) {
            return ResponseDto.fail("입력값을 다시 확인해주세요");
        } else if (userId.trim().isEmpty() || password.trim().isEmpty() || nickname.trim().isEmpty()) {
            return ResponseDto.fail("빈칸을 채워주세요");
        }
        // 비밀번호 및 비밀번호 확인 일치 검사
        if (!password.equals(passwordCheck))
            return ResponseDto.fail("두 비밀번호가 일치하지 않습니다");
        // 아이디 중복검사
        if (!isPresentId(userId).isSuccess())
            return ResponseDto.fail("이미 존재하는 아이디 입니다.");
        // 닉네임 중복검사
        if (!isPresentNickname(nickname).isSuccess())
            return ResponseDto.fail("이미 존재하는 닉네임 입니다.");
        // 이미지를 업로드 하지 않을 시 기본 이미지 설정
        if (imgFile.isEmpty()) {
            imgUrl = "https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/memberImage/6c6c20cf-7490-4d9e-b6f6-73c185a417dd%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.webp";
        } else {
            // 이미지 업로드 관련 로직
            ResponseDto<?> image = amazonS3Service.uploadFile(imgFile);
            ImageFile imageFile = (ImageFile) image.getData();
            imgUrl = imageFile.getUrl();
        }


        Member member = Member.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .img_url(imgUrl)
                .userRole(Authority.ROLE_MEMBER)
                .build();

        memberRepository.save(member);
        return ResponseDto.success(member);
    }


    // 회원 아이디 중복 검사 method
    public ResponseDto<String> isPresentId(String id) {
        Optional<Member> userId = memberRepository.findByUserId(id);
        if (userId.isPresent()) {
            return ResponseDto.fail("이미 존재하는 회원 아이디입니다.");
        } else return ResponseDto.success("사용 가능한 아이디입니다.");
    }

    // 닉네임 중복 검사 method
    public ResponseDto<String> isPresentNickname(String nickname) {
        Optional<Member> findNickname = memberRepository.findByNickname(nickname);
        if (findNickname.isPresent()) {
            return ResponseDto.fail("이미 존재하는 닉네임입니다.");
        } else return ResponseDto.success("사용 가능한 닉네임입니다.");

    }
}
