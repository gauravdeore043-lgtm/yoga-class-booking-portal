package com.yoga.portal.web;

import com.yoga.portal.model.Booking;
import com.yoga.portal.service.BookingException;
import com.yoga.portal.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public String requestBooking(@RequestParam("slotId") Long slotId, HttpSession session, RedirectAttributes redirect) {
        Long memberId = SessionKeys.memberId(session);
        if (memberId == null) {
            redirect.addFlashAttribute("error", "Please register or sign in before booking a class.");
            return "redirect:/register";
        }
        try {
            Booking booking = bookingService.requestBooking(memberId, slotId);
            redirect.addFlashAttribute("success",
                    "Booking requested. Your reference is " + booking.getReference() + ".");
            return "redirect:/bookings/" + booking.getReference();
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/slots";
        }
    }

    @GetMapping("/bookings/{reference}")
    public String bookingDetail(@PathVariable String reference, HttpSession session, Model model,
                                RedirectAttributes redirect) {
        Optional<Booking> booking = bookingService.findByReference(reference);
        if (booking.isEmpty()) {
            redirect.addFlashAttribute("error", "No booking found with reference " + reference + ".");
            return "redirect:/slots";
        }
        model.addAttribute("booking", booking.get());
        model.addAttribute("isOwner", booking.get().isOwnedBy(SessionKeys.memberId(session)));
        return "booking-detail";
    }
}
