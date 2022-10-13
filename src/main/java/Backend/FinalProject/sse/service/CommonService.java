package Backend.FinalProject.sse.service;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.UserDetailsImpl;
import Backend.FinalProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonService {
    private final MemberRepository memberRepository;

    public Member getUser() {
        log.info("===========================1-1고승유=====================================================");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

        String username = principal.getMember().getNickname();
        log.info("===========================1-2고승유=====================================================");

        return memberRepository.findByNickname(username).orElseThrow(
                () -> new UsernameNotFoundException("존재하지 않는 유저입니다")
        );
    }
}
