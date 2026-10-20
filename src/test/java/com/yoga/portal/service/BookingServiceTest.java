package com.yoga.portal.service;

import com.yoga.portal.model.Booking;
import com.yoga.portal.model.BookingStatus;
import com.yoga.portal.model.Member;
import com.yoga.portal.model.YogaSlot;
import com.yoga.portal.repository.YogaSlotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private YogaSlotRepository slots;

    private Member newMember() {
        return memberService.register("Test Member", "member" + System.nanoTime() + "@example.com", "9876543210");
    }

    private YogaSlot newSlot(int capacity) {
        return slots.save(new YogaSlot("Test Class", "Instructor", "All levels",
                LocalDateTime.now().plusDays(1), 60, capacity));
    }

    @Test
    void requestBookingHoldsSeatAndIssuesReference() {
        Member member = newMember();
        YogaSlot slot = newSlot(5);

        Booking booking = bookingService.requestBooking(member.getId(), slot.getId());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(booking.getReference()).matches("YB-[A-Z0-9]{6}");
        assertThat(slots.findById(slot.getId()).orElseThrow().getSeatsLeft()).isEqualTo(4);
    }

    @Test
    void duplicateActiveBookingIsRejected() {
        Member member = newMember();
        YogaSlot slot = newSlot(5);
        bookingService.requestBooking(member.getId(), slot.getId());

        assertThatThrownBy(() -> bookingService.requestBooking(member.getId(), slot.getId()))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already have an active booking");
    }

    @Test
    void fullClassCannotBeBooked() {
        YogaSlot slot = newSlot(1);
        bookingService.requestBooking(newMember().getId(), slot.getId());

        assertThatThrownBy(() -> bookingService.requestBooking(newMember().getId(), slot.getId()))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("fully booked");
    }

    @Test
    void duplicateEmailRegistrationIsRejected() {
        String email = "dup" + System.nanoTime() + "@example.com";
        memberService.register("First", email, "9876543210");

        assertThatThrownBy(() -> memberService.register("Second", email.toUpperCase(), "9876543210"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already exists");
    }
}
