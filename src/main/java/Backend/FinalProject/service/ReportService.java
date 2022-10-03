package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Report;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.ReportDto;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final Validation validation;

    @Transactional
    public  ResponseDto<?> reportUser(Long memberId, ReportDto reportDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

        // 신고할 회원 찾기
        Member reportMember = memberRepository.findById(memberId).orElse(null);
        if (reportMember == null) {
            return ResponseDto.fail("NOT FOUND", "해당 회원을 찾을 수 없습니다.");
        }

        Report report = Report.builder()
                .content(reportDto.getContent())
                .memberId(reportMember)
                .build();

        reportRepository.save(report);

        return ResponseDto.success("신고 접수가 완료되었습니다.");


    }
}
