package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.*;
import Backend.FinalProject.domain.enums.*;
import Backend.FinalProject.dto.ResignDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.TokenDto;
import Backend.FinalProject.dto.request.member.LoginRequestDto;
import Backend.FinalProject.dto.request.member.MemberUpdateDto;
import Backend.FinalProject.dto.request.member.SignupRequestDto;
import Backend.FinalProject.dto.response.member.MemberPasswordUpdateDto;
import Backend.FinalProject.dto.response.member.ReIssueMessageDto;
import Backend.FinalProject.repository.*;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static Backend.FinalProject.Tool.Validation.handleBoolean;
import static Backend.FinalProject.Tool.Validation.handleNull;
import static Backend.FinalProject.domain.enums.AgeCheck.CHECKED;
import static Backend.FinalProject.domain.enums.AgeCheck.UNCHECKED;
import static Backend.FinalProject.domain.enums.Authority.ROLE_MEMBER;
import static Backend.FinalProject.domain.enums.ErrorCode.*;
import static Backend.FinalProject.domain.enums.MarketingAgreement.MARKETING_AGREE;
import static Backend.FinalProject.domain.enums.MarketingAgreement.MARKETING_DISAGREE;
import static Backend.FinalProject.domain.enums.Regulation.REGULATED;
import static Backend.FinalProject.domain.enums.Regulation.UNREGULATED;
import static Backend.FinalProject.domain.enums.RequiredAgreement.REQUIRED_AGREE;
import static Backend.FinalProject.domain.enums.RequiredAgreement.REQUIRED_DISAGREE;
import static Backend.FinalProject.domain.enums.SignUpRoot.normal;

@Slf4j
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
    private final SignOutRepository signOutRepository;
    private final PostRepository postRepository;
    private final EntityManager em;

    private final Validation validation;

    // ???????????? ??? ?????? ????????? ????????? ?????? ?????? ?????? ????????? ??????
    String baseImage = "https://tommy-bucket-final.s3.ap-northeast-2.amazonaws.com/memberImage/6c6c20cf-7490-4d9e-b6f6-73c185a417dd%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.webp";
    // S3??? ?????? ????????? ?????? ?????? ??????
    String folderName = "/memberImage";

    // ????????? ??? ???????????? ????????? ??????
    String regexpPassword = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,16}$";
    String regexpId="^(?=.*[a-zA-Z]).{5,12}$";


    @Transactional
    public ResponseDto<Object> createMember(SignupRequestDto request) {

        // null ??? ??? ????????? ?????? ??? ????????????
        ResponseDto<Object> checkNull = handleBoolean(request.getUserId() == null
                || request.getPassword() == null || request.getNickname() == null, MEMBER__NULL_DATA);
        if (checkNull != null) return checkNull;
        ResponseDto<Object> checkEmpty = handleBoolean(request.getUserId().trim().isEmpty()
                || request.getPassword().trim().isEmpty() || request.getNickname().trim().isEmpty(), MEMBER_EMPTY_DATA);
        if (checkEmpty != null) return checkEmpty;

        ResponseDto<Object> checkPassword = handleBoolean(!Pattern.matches(regexpPassword, request.getPassword()), MEMBER_INVALID_PASSWORD);
        if (checkPassword != null) return checkPassword;

        ResponseDto<Object> checkId = handleBoolean(!Pattern.matches(regexpId, request.getUserId()), MEMBER_INVALID_ID);
        if(checkId !=null) return checkId;


        // ???????????? ??? ???????????? ?????? ?????? ??????
        ResponseDto<Object> doubleCheck = handleBoolean(!request.getPassword().equals(request.getPasswordCheck()), MEMBER_DOUBLE_CHECK);
        if (doubleCheck != null) return doubleCheck;

        // ????????? ????????????
        ResponseDto<Object> checkExisId = handleBoolean(!checkDuplicateId(request.getUserId()).isSuccess(), MEMBER_ALREADY_EXIST_ID);
        if (checkExisId !=null) return checkExisId;
        // ????????? ????????????
        ResponseDto<Object> checkExistNickname = handleBoolean(!checkDuplicateNickname(request.getNickname()).isSuccess(), MEMBER_ALREADY_EXIST_NICKNAME);
        if (checkExistNickname != null) return checkExistNickname;
        ResponseDto<Object> checkRequiredAgreement = handleBoolean(request.getRequiredAgreement() == null || request.getRequiredAgreement().equals("false"), MEMBER_REQUIRED_AGREEMENT);
        if (checkRequiredAgreement !=null) return checkRequiredAgreement;
        handleBoolean(request.getAgeCheck() == null || request.getAgeCheck().equals("false"), MEMBER_REQUIRED_AGREEMENT);

        String imgUrl;

        // ???????????? ????????? ?????? ?????? ??? ?????? ????????? ??????
        if (request.getImgFile() == null || request.getImgFile().isEmpty()) {
            imgUrl = baseImage;
        } else {
            // ????????? ????????? ?????? ??????
            ResponseDto<?> image = amazonS3Service.uploadFile(request.getImgFile(), folderName);
            ImageFile imageFile = (ImageFile) image.getData();
            imgUrl = imageFile.getUrl();
        }

        RequiredAgreement setRequiredAgreement = setRequiredAgreement(request);
        MarketingAgreement setMarketingAgreement = setMarketingAgreement(request);
        AgeCheck setAgeCheck = setAgeCheck(request);

        int minAge;
        minAge = setAgeRange(request.getAge());
        Gender genderSet = setGender(request);
        Member member = makeMember(request, imgUrl, minAge, genderSet, setAgeCheck, setRequiredAgreement, setMarketingAgreement);
        memberRepository.save(member);
        return ResponseDto.success(member.getUserId() + "??? ???????????? ??????");
    }

    @Transactional
    public ResponseDto<Object> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        Member member = isPresentMember(loginRequestDto.getUserId());
        ResponseDto<Object> checkNullId = handleNull(loginRequestDto.getUserId(), MEMBER_EMPTY_DATA);
        if (checkNullId != null) return checkNullId;
        ResponseDto<Object> handleNullPassword = handleNull(loginRequestDto.getPassword(), MEMBER_EMPTY_DATA);
        if (handleNullPassword!=null) return handleNullPassword;
        ResponseDto<Object> memberCheck = handleNull(member, MEMBER_DO_NOT_EXIST);
        if (memberCheck != null) return memberCheck;
        ResponseDto<Object> checkPassword = handleBoolean(!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword()), MEMBER_INVALID_PASSWORD);
        if (checkPassword != null) return checkPassword;
        ResponseDto<Object> checkRegulation = handleBoolean(member.getRegulation() == REGULATED, MEMBER_REGULATED);
        if (checkRegulation != null) return checkRegulation;

        // ?????? ??????
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        // ????????? ?????? ??????
        addHeader(response, member, tokenDto);

        return ResponseDto.success(member.getUserId() + "??? ????????? ??????!");
    }

    @Transactional
    public ResponseDto<?> editProfile(MemberUpdateDto request, HttpServletRequest httpServletRequest,
                                      HttpServletResponse response) {
        String imgUrl;

        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        Member findMember = memberRepository.findById(member.getId()).get();

        ResponseDto<Object> checkNull = handleNull(request.getNickname(), MEMBER__NULL_DATA);
        if (checkNull != null) return checkNull;
        ResponseDto<Object> checkEmpty = handleBoolean(request.getNickname().trim().isEmpty(), MEMBER_EMPTY_DATA);
        if (checkEmpty != null) return checkEmpty;
        findMember.updateNickname(request.getNickname());

        // ?????? ??????
        TokenDto tokenDto = tokenProvider.generateTokenDto(findMember);
        addHeader(response, findMember, tokenDto);

        if (request.getImgFile() == null || request.getImgFile().isEmpty()) {
            return ResponseDto.success("??????????????? ?????????????????????.");
        }
        uploadImage(request.getImgFile(), member, findMember);
        return ResponseDto.success("??????????????? ?????? ????????? ?????????????????????");
    }

    @Transactional
    public ResponseDto<?> updatePassword(MemberPasswordUpdateDto request, HttpServletRequest httpServletRequest) {
        String password = request.getPassword();
        String updatePassword = request.getUpdatePassword();
        String UpdatePasswordCheck = request.getUpdatePasswordCheck();

        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        Member findMember = memberRepository.findById(member.getId()).get();

        if (!passwordEncoder.matches(password, member.getPassword())) {
            log.info("MemberService updateMemberPassword PASSWORD_ERROR");
            return ResponseDto.fail("PASSWORD_ERROR", "?????? ??????????????? ???????????? ????????????");
        }
        if (!Pattern.matches(regexpPassword, updatePassword)) {
            return ResponseDto.fail("INVALID TYPE", "???????????? ????????? ???????????????");
        }

        if (password == null || updatePassword == null || UpdatePasswordCheck == null) {
            log.info("MemberService updateMemberPassword NULL_DATA");
            return ResponseDto.fail("NULL_DATA", "???????????? ?????? ??????????????????");
        } else if (password.trim().isEmpty() || updatePassword.trim().isEmpty() || UpdatePasswordCheck.trim().isEmpty()) {
            log.info("MemberService updateMemberPassword EMPTY_DATA");
            return ResponseDto.fail("EMPTY_DATA", "????????? ???????????????");
        }

        if (!updatePassword.equals(UpdatePasswordCheck)) {
            log.info("MemberService updateMemberPassword DOUBLE-CHECK_ERROR");
            return ResponseDto.fail("DOUBLE-CHECK_ERROR", "??? ??????????????? ???????????? ????????????");
        }

        findMember.updatePassword(passwordEncoder.encode(updatePassword));

        return ResponseDto.success("???????????? ????????? ?????????????????????");
    }

    @Transactional
    public ResponseDto<?> logout(HttpServletRequest request) {

        String refreshToken = request.getHeader("RefreshToken");
        RefreshToken validateToken = refreshTokenRepository.findByKeyValue(refreshToken).orElse(null);

        log.info(String.valueOf(validateToken));
        ResponseDto<Object> checkLogout = handleNull(validateToken, MEMBER_ALREADY_LOGOUT);
        if (checkLogout !=null) return checkLogout;

        ResponseDto<Object> checkToken = handleBoolean(!tokenProvider.validateToken(request.getHeader("RefreshToken")), MEMBER_INVALID_TOKEN);
        if (checkToken !=null) return checkToken;


//        if (validateToken == null) {
//            log.info("MemberService logout ALREADY LOGOUT");
//            return ResponseDto.fail("ALREADY LOGOUT", "?????? ???????????? ???????????????.");
//        }

//        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
//            log.info("MemberService logout INVALID TOKEN");
//            return ResponseDto.fail("INVALID TOKEN", "?????? ?????? ???????????? ????????????.");
//        }

        // ???????????? ????????????
        Member member = tokenProvider.getMemberFromAuthentication();
        ResponseDto<Object> checkLogin = handleNull(member, MEMBER_LOGIN_NOT_FOUND);
        if (checkLogin != null) return checkLogin;
//        if (null == member) {
//            log.info("MemberService logout MEMBER_NOT_FOUND");
//            return ResponseDto.fail("MEMBER_NOT_FOUND", "???????????? ?????? ??? ????????????.");
//        }
        tokenProvider.deleteRefreshToken(member);


        return ResponseDto.success("???????????? ??????");
    }

    @Transactional
    public ResponseDto<?> signOut(HttpServletRequest request) {
        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        ResponseDto<Object> checkMember = handleNull(member, MEMBER_DO_NOT_EXIST);
        if (checkMember != null) return checkMember;

        if (!member.getImgUrl().equals(baseImage)) {
            ImageFile deleteImage = fileRepository.findByUrl(member.getImgUrl());
            amazonS3Service.removeFile(deleteImage.getImageName(), folderName);
        }
        // ????????? ????????? ????????? ????????????
        closePost(member);


        refreshTokenRepository.deleteById(member.getUserId());

        SignOutMember signOutMember = makeSignOutMemeber(member);
        signOutRepository.save(signOutMember);

        member.signOut();
        em.merge(member);
        return ResponseDto.success("?????? ????????? ??????????????? ?????????????????????.");
    }



    // ?????? ????????? ?????? ?????? method
    public ResponseDto<String> checkDuplicateId(String id) {
        Optional<Member> userId = memberRepository.findByUserId(id);
        if (!Pattern.matches(regexpId,id)) {
            return ResponseDto.fail("INVALID ID", "????????? ????????? ?????? ??????????????????");
        }
        if (userId.isPresent()) {
            log.info("MemberService isPresentId ALREADY EXIST-ID");
            return ResponseDto.fail("ALREADY EXIST-ID", "?????? ???????????? ?????? ??????????????????.");
        } else if (id == null) {
            return ResponseDto.fail("INVALID TYPE", "???????????? ????????? ???????????? ??????????????????");
        } else {
            return ResponseDto.success("?????? ????????? ??????????????????.");
        }
    }

    // ????????? ?????? ?????? method
    public ResponseDto<String> checkDuplicateNickname(String nickname) {
        Optional<Member> findNickname = memberRepository.findByNickname(nickname);
        if (findNickname.isPresent()) {
            log.info("MemberService isPresentId ALREADY EXIST-NICKNAME");
            return ResponseDto.fail("ALREADY EXIST-NICKNAME", "?????? ???????????? ??????????????????.");
        } else if (nickname == null) {
            return ResponseDto.fail("INVALID TYPE", "???????????? ????????? ???????????? ??????????????????");
        } else {
            return ResponseDto.success("?????? ????????? ??????????????????.");
        }
    }

    // ?????? ??????
    public Member isPresentMember(String userId) {
        Optional<Member> findMember = memberRepository.findByUserId(userId);
        return findMember.orElse(null);
    }

    @Transactional
    public ResponseDto<?> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) throws ParseException {
            if (tokenProvider.getUserIdByToken(request.getHeader("Authorization")) != null) {
                Date expirationTime = tokenProvider.getExpirationTime(request.getHeader("Authorization"));
                Date now = new Date(System.currentTimeMillis());
                long diff = expirationTime.getTime() - now.getTime();
                long restTime = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.MILLISECONDS);
                return ResponseDto.success(ReIssueMessageDto.builder().message("?????? ????????? ???????????????.").expiresAt(restTime).build());
            }
            if (!tokenProvider.validateToken((request.getHeader("RefreshToken")))) {
                return ResponseDto.fail("INVALID REFRESH TOKEN", "RefreshToken ??? ???????????? ????????????.");
            }
            String memberId = tokenProvider.getMemberFromExpiredAccessToken(request);           // ???????????? ?????????
            if (null == memberId) {
                return ResponseDto.fail("INCORRECT ACESSTOKEN", "Access Token ?????? ???????????? ????????????.");
            }
            Member member = memberRepository.findByNickname(memberId).orElse(null);

            RefreshToken refreshToken = tokenProvider.isPresentRefreshToken(member);
            if (refreshToken == null) return ResponseDto.fail("NEED LOGIN", "??? ????????? ??????????????????.");

            if (!refreshToken.getKeyValue().equals(request.getHeader("RefreshToken"))) {
                return ResponseDto.fail("INVALID REFRESH TOKEN", "????????? ???????????? ????????????.");
            }
            assert member != null;
            TokenDto tokenDto = tokenProvider.generateTokenDto(member);
            refreshToken.updateValue(tokenDto.getRefreshToken());

            tokenToHeaders(tokenDto, response);
            return ResponseDto.success("????????? ??????");
    }
    @Transactional
    public ResponseDto<?> rejoin(ResignDto resignDto) {
        SignOutMember signOutMember = signOutRepository.findByUserId(resignDto.getUserId()).orElse(null);
        ResponseDto<Object> checkSignOutMember = handleNull(signOutMember, MEMBER_NOT_FOUND);
        if (checkSignOutMember != null) return checkSignOutMember;

        ResponseDto<Object> checkPassword = handleBoolean(!passwordEncoder.matches(resignDto.getPassword(), signOutMember.getPassword()), MEMBER_PASSWORD_NOT_MATCH);
        if (checkPassword != null) return checkPassword;

        Member member = memberRepository.findByUserId(resignDto.getUserId()).orElse(null);
        member.rejoin(signOutMember.getNickname(), signOutMember.getMinAge(), signOutMember.getImgUrl(), signOutMember.getRegulation());
        return ResponseDto.success("???????????? ?????????????????????.");
    }

    // ????????? ????????????
    public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
    }

    private static void addHeader(HttpServletResponse response, Member member, TokenDto tokenDto) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("ImgUrl", member.getImgUrl());
        response.addHeader("Id", member.getUserId());
        response.addHeader("role", String.valueOf(member.getUserRole()));
    }

    // ?????? ?????? ??????

    private static int setAgeRange(Integer age) {
        int minAge;
        if (age < 20) {
            minAge = 10;
        } else if (age < 30) {
            minAge = 20;
        } else if (age < 40) {
            minAge = 30;
        } else if (age < 50) {
            minAge = 40;
        } else {
            minAge = 50;
        }
        return minAge;
    }
    private Member makeMember(SignupRequestDto request, String imgUrl, int minAge, Gender genderSet, AgeCheck setAgeCheck, RequiredAgreement setRequiredAgreement, MarketingAgreement setMarketingAgreement) {
        Member member = Member.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .imgUrl(imgUrl)
                .userRole(ROLE_MEMBER)
                .root(normal)
                .gender(genderSet)
                .minAge(minAge)
                .ageCheck(setAgeCheck)
                .requiredAgreement(setRequiredAgreement)
                .marketingAgreement(setMarketingAgreement)
                .regulation(UNREGULATED)
                .build();
        return member;
    }

    private void uploadImage(MultipartFile imgFile, Member member, Member findMember) {
        String imgUrl;
        if (!imgFile.isEmpty()) {
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
    }
    private static RequiredAgreement setRequiredAgreement(SignupRequestDto request) {
        RequiredAgreement setRequiredAgreement = REQUIRED_DISAGREE;
        if (request.getRequiredAgreement().equals("true")) {
            setRequiredAgreement = REQUIRED_AGREE;
        }
        return setRequiredAgreement;
    }
    private static MarketingAgreement setMarketingAgreement(SignupRequestDto request) {
        MarketingAgreement setMarketingAgreement = MARKETING_DISAGREE;
        if (request.getMarketingAgreement() == null) {
            setMarketingAgreement = MARKETING_DISAGREE;
        } else if (request.getMarketingAgreement().equals("true")) {
            setMarketingAgreement = MARKETING_AGREE;
        }
        return setMarketingAgreement;
    }

    private static AgeCheck setAgeCheck(SignupRequestDto request) {
        AgeCheck setAgeCheck = UNCHECKED;
        if (request.getAgeCheck().equals("true")) {
            setAgeCheck = CHECKED;
        }
        return setAgeCheck;
    }

    private static Gender setGender(SignupRequestDto request) {
        Gender genderSet = Gender.NEUTRAL;
        if (request.getGender().equals("male")) {
            genderSet = Gender.MALE;
        } else if (request.getGender().equals("female")) {
            genderSet = Gender.FEMALE;
        }
        return genderSet;
    }

    private static SignOutMember makeSignOutMemeber(Member member) {
        SignOutMember signOutMember = SignOutMember.builder()
                .userId(member.getUserId())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .minAge(member.getMinAge())
                .imgUrl(member.getImgUrl())
                .regulation(member.getRegulation())
                .build();
        return signOutMember;
    }

    private void closePost(Member member) {
        List<Post> AllPost = postRepository.findAllByMemberId(member.getId());
        for (Post post : AllPost) {
            if (post.getStatus() == PostState.RECRUIT) {
                post.disclose();
            }
        }
    }
}
