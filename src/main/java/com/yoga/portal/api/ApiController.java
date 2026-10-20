package com.yoga.portal.api;

import com.yoga.portal.model.Booking;
import com.yoga.portal.model.YogaSlot;
import com.yoga.portal.repository.YogaSlotRepository;
import com.yoga.portal.service.BookingService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** JSON API used by the front desk, monitoring and the deployment health checks. */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final YogaSlotRepository slots;
    private final BookingService bookingService;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final String environment;

    public ApiController(YogaSlotRepository slots, BookingService bookingService,
                         ObjectProvider<BuildProperties> buildProperties,
                         @Value("${app.environment:local}") String environment) {
        this.slots = slots;
        this.bookingService = bookingService;
        this.buildProperties = buildProperties;
        this.environment = environment;
    }

    public record SlotView(Long id, String title, String instructor, String level,
                           LocalDateTime startTime, int capacity, int seatsLeft) {
        static SlotView of(YogaSlot s) {
            return new SlotView(s.getId(), s.getTitle(), s.getInstructor(), s.getLevel(),
                    s.getStartTime(), s.getCapacity(), s.getSeatsLeft());
        }
    }

    public record BookingView(String reference, String status, String member, String slot,
                              LocalDateTime startTime, LocalDateTime updatedAt) {
        static BookingView of(Booking b) {
            return new BookingView(b.getReference(), b.getStatus().name(), b.getMember().getName(),
                    b.getSlot().getTitle(), b.getSlot().getStartTime(), b.getUpdatedAt());
        }
    }

    @GetMapping("/slots")
    @Transactional(readOnly = true)
    public List<SlotView> slots() {
        return slots.findAllByOrderByStartTimeAsc().stream().map(SlotView::of).toList();
    }

    @GetMapping("/bookings/{reference}")
    public ResponseEntity<BookingView> booking(@PathVariable String reference) {
        return bookingService.findByReference(reference)
                .map(b -> ResponseEntity.ok(BookingView.of(b)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        BuildProperties props = buildProperties.getIfAvailable();
        return Map.of(
                "app", "yoga-portal",
                "version", props != null ? props.getVersion() : "dev",
                "environment", environment);
    }
}
