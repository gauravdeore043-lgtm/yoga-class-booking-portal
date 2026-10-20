package com.yoga.portal.web;

import com.yoga.portal.model.Booking;
import com.yoga.portal.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/** Public status tracking by booking reference (for members and front desk). */
@Controller
public class StatusController {

    private final BookingService bookingService;

    public StatusController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/status")
    public String status(@RequestParam(value = "ref", required = false) String ref, Model model) {
        model.addAttribute("ref", ref == null ? "" : ref.trim());
        if (ref != null && !ref.isBlank()) {
            Optional<Booking> booking = bookingService.findByReference(ref);
            model.addAttribute("searched", true);
            booking.ifPresent(b -> model.addAttribute("booking", b));
        }
        return "status";
    }
}
