package com.yoga.portal.web;

import com.yoga.portal.model.Member;
import com.yoga.portal.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds values every page needs (signed-in member, version, environment) to the model. */
@ControllerAdvice(basePackages = "com.yoga.portal.web")
public class GlobalModelAdvice {

    private final ObjectProvider<BuildProperties> buildProperties;
    private final MemberService memberService;
    private final String environment;

    public GlobalModelAdvice(ObjectProvider<BuildProperties> buildProperties,
                             MemberService memberService,
                             @Value("${app.environment:local}") String environment) {
        this.buildProperties = buildProperties;
        this.memberService = memberService;
        this.environment = environment;
    }

    @ModelAttribute("currentMember")
    public Member currentMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return memberService.findById(SessionKeys.memberId(session)).orElse(null);
    }

    @ModelAttribute("appVersion")
    public String appVersion() {
        BuildProperties props = buildProperties.getIfAvailable();
        return props != null ? props.getVersion() : "dev";
    }

    @ModelAttribute("appEnvironment")
    public String appEnvironment() {
        return environment;
    }
}
