package com.yoga.portal.model;

import com.yoga.portal.service.BookingException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String reference;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = false)
    private YogaSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Booking() {
    }

    public Booking(String reference, Member member, YogaSlot slot) {
        this.reference = reference;
        this.member = member;
        this.slot = slot;
        this.status = BookingStatus.REQUESTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** REQUESTED -> CONFIRMED. */
    public void confirm() {
        if (status != BookingStatus.REQUESTED) {
            throw new BookingException(
                    "Only a requested booking can be confirmed (current status: " + status + ").");
        }
        status = BookingStatus.CONFIRMED;
        updatedAt = LocalDateTime.now();
    }

    /** REQUESTED or CONFIRMED -> CANCELLED. */
    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new BookingException("This booking is already cancelled.");
        }
        status = BookingStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status != BookingStatus.CANCELLED;
    }

    public boolean isOwnedBy(Long memberId) {
        return memberId != null && memberId.equals(member.getId());
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public Member getMember() { return member; }
    public YogaSlot getSlot() { return slot; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
