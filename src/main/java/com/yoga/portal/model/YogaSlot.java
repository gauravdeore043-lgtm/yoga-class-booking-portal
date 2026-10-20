package com.yoga.portal.model;

import com.yoga.portal.service.BookingException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** One scheduled class with a fixed capacity. */
@Entity
@Table(name = "yoga_slots")
public class YogaSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 80)
    private String instructor;

    @Column(nullable = false, length = 30)
    private String level;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int bookedCount;

    protected YogaSlot() {
    }

    public YogaSlot(String title, String instructor, String level,
                    LocalDateTime startTime, int durationMinutes, int capacity) {
        this.title = title;
        this.instructor = instructor;
        this.level = level;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
        this.bookedCount = 0;
    }

    public int getSeatsLeft() {
        return capacity - bookedCount;
    }

    public boolean isFull() {
        return getSeatsLeft() <= 0;
    }

    /** Holds one seat. Never lets the class go over capacity. */
    public void reserveSeat() {
        if (isFull()) {
            throw new BookingException("Sorry, " + title + " is fully booked.");
        }
        bookedCount++;
    }

    /** Gives one seat back. Never goes below zero. */
    public void releaseSeat() {
        if (bookedCount > 0) {
            bookedCount--;
        }
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getInstructor() { return instructor; }
    public String getLevel() { return level; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getCapacity() { return capacity; }
    public int getBookedCount() { return bookedCount; }
}
