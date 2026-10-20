package com.yoga.portal.service;

import com.yoga.portal.model.Booking;
import com.yoga.portal.model.BookingStatus;
import com.yoga.portal.model.Member;
import com.yoga.portal.model.YogaSlot;
import com.yoga.portal.repository.BookingRepository;
import com.yoga.portal.repository.MemberRepository;
import com.yoga.portal.repository.YogaSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private static final String REF_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookings;
    private final MemberRepository members;
    private final YogaSlotRepository slots;

    public BookingService(BookingRepository bookings, MemberRepository members, YogaSlotRepository slots) {
        this.bookings = bookings;
        this.members = members;
        this.slots = slots;
    }

    /** Creates a REQUESTED booking and holds one seat in the class. */
    public Booking requestBooking(Long memberId, Long slotId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new BookingException("Please register or sign in before booking."));
        YogaSlot slot = slots.findById(slotId)
                .orElseThrow(() -> new BookingException("That class no longer exists."));

        boolean alreadyBooked = bookings.existsByMemberIdAndSlotIdAndStatusIn(
                memberId, slotId, EnumSet.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED));
        if (alreadyBooked) {
            throw new BookingException("You already have an active booking for " + slot.getTitle() + ".");
        }

        slot.reserveSeat();
        return bookings.save(new Booking(newReference(), member, slot));
    }

    /** Confirms a REQUESTED booking owned by the member. */
    public Booking confirm(String reference, Long memberId) {
        Booking booking = ownedBooking(reference, memberId);
        booking.confirm();
        return booking;
    }

    /** Cancels a booking owned by the member and gives the seat back to the class. */
    public Booking cancel(String reference, Long memberId) {
        Booking booking = ownedBooking(reference, memberId);
        booking.cancel();
        booking.getSlot().releaseSeat();
        return booking;
    }

    private Booking ownedBooking(String reference, Long memberId) {
        Booking booking = findByReference(reference)
                .orElseThrow(() -> new BookingException("No booking found with reference " + reference + "."));
        if (!booking.isOwnedBy(memberId)) {
            throw new BookingException("You can only manage your own bookings. Please sign in as the member who booked.");
        }
        return booking;
    }

    @Transactional(readOnly = true)
    public Optional<Booking> findByReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return Optional.empty();
        }
        return bookings.findByReference(reference.trim().toUpperCase(Locale.ROOT));
    }

    @Transactional(readOnly = true)
    public List<Booking> bookingsFor(Long memberId) {
        return bookings.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    private String newReference() {
        String ref;
        do {
            StringBuilder sb = new StringBuilder("YB-");
            for (int i = 0; i < 6; i++) {
                sb.append(REF_ALPHABET.charAt(RANDOM.nextInt(REF_ALPHABET.length())));
            }
            ref = sb.toString();
        } while (bookings.existsByReference(ref));
        return ref;
    }
}
