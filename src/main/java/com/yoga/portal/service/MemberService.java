package com.yoga.portal.service;

import com.yoga.portal.model.Member;
import com.yoga.portal.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository members;

    public MemberService(MemberRepository members) {
        this.members = members;
    }

    public Member register(String name, String email, String phone) {
        String normalisedEmail = normaliseEmail(email);
        if (members.existsByEmailIgnoreCase(normalisedEmail)) {
            throw new BookingException("An account with this email already exists. Please sign in instead.");
        }
        return members.save(new Member(name.trim(), normalisedEmail, phone.trim()));
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return members.findByEmailIgnoreCase(normaliseEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return id == null ? Optional.empty() : members.findById(id);
    }

    private static String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
