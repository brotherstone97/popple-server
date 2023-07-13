package com.popple.server.domain.user.service;

import com.popple.server.domain.entity.Member;
import com.popple.server.domain.user.dto.KakaoLoginRequestDto;
import com.popple.server.domain.user.repository.MemberRepository;
import com.popple.server.domain.user.dto.CreateUserRequestDto;
import com.popple.server.domain.user.dto.CreateUserResponseDto;
import com.popple.server.domain.user.exception.AlreadyExistException;
import com.popple.server.domain.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void checkExistProceed(String email) {
        Member findMember = memberRepository.findByEmail(email);

        if (findMember != null) {
            throw new AlreadyExistException(UserErrorCode.PROCEEDING_EMAIL);
        }
    }

    @Transactional
    public Member createKakaoMember(KakaoLoginRequestDto kakaoLoginRequestDto) {
        Member kakaoMember = Member.builder()
                .email(kakaoLoginRequestDto.getEmail())
                .nickname(kakaoLoginRequestDto.getNickname())
                .password(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        return memberRepository.save(kakaoMember);
    }

    @Transactional
    public CreateUserResponseDto createWithPassword(final CreateUserRequestDto createUserRequestDto) {

        String email = createUserRequestDto.getEmail();
        Member findMember = memberRepository.findByEmail(email);
        if (findMember != null) {
            throw new AlreadyExistException(UserErrorCode.EXIST_EMAIL);
        }

        String encodedPassword = bCryptPasswordEncoder.encode(createUserRequestDto.getPassword());

        createUserRequestDto.setPassword(encodedPassword);
        Member member = createUserRequestDto.toEntity();
        memberRepository.save(member);

        return CreateUserResponseDto.from(member);
    }

    public void checkDuplication(String nickname, String email) {

        if (memberRepository.existsByEmail(email)) {
            throw new RuntimeException();
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new RuntimeException();
        }
    }

    public Member getOptionalUserByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Member getUser(String email) {
        Member findMember = memberRepository.findByEmail(email);

        if (findMember == null) {
            throw new RuntimeException();
        }

        return findMember;
    }

    public Member getUser(String email, String password) {
        Member findMember = memberRepository.findByEmail(email);

        if (findMember == null) {
            throw new RuntimeException();
        }

        if (!bCryptPasswordEncoder.matches(password, findMember.getPassword())) {
            throw new RuntimeException();
        }

        return findMember;
    }
}