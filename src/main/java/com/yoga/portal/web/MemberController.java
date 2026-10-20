package com.yoga.portal.web;

import com.yoga.portal.model.Member;
import com.yoga.portal.service.BookingException;
import com.yoga.portal.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            Member member = memberService.register(form.getName(), form.getEmail(), form.getPhone());
            session.setAttribute(SessionKeys.MEMBER_ID, member.getId());
            redirect.addFlashAttribute("success",
                    "Welcome, " + member.getName() + "! Your member account is ready. Pick a class below.");
            return "redirect:/slots";
        } catch (BookingException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, HttpSession session, RedirectAttributes redirect) {
        Optional<Member> member = memberService.findByEmail(email);
        if (member.isEmpty()) {
            redirect.addFlashAttribute("error", "No member found with that email. Please register first.");
            return "redirect:/register";
        }
        session.setAttribute(SessionKeys.MEMBER_ID, member.get().getId());
        redirect.addFlashAttribute("success", "Welcome back, " + member.get().getName() + "!");
        return "redirect:/slots";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("success", "You have been signed out.");
        return "redirect:/";
    }
}
