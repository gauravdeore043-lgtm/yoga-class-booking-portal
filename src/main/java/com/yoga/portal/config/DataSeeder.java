package com.yoga.portal.config;

import com.yoga.portal.model.YogaSlot;
import com.yoga.portal.repository.YogaSlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Seeds the in-memory database with this week's class timetable. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final YogaSlotRepository slots;

    public DataSeeder(YogaSlotRepository slots) {
        this.slots = slots;
    }

    @Override
    public void run(String... args) {
        if (slots.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        slots.saveAll(List.of(
                slot("Sunrise Hatha Flow", "Asha Menon", "Beginner", today.plusDays(1), 6, 30, 60, 12),
                slot("Power Vinyasa", "Rohan Kulkarni", "Intermediate", today.plusDays(1), 18, 0, 60, 15),
                slot("Yin & Restore", "Meera Iyer", "All levels", today.plusDays(2), 19, 0, 75, 10),
                slot("Pranayama & Meditation", "Dev Sharma", "All levels", today.plusDays(3), 7, 0, 45, 20),
                slot("Ashtanga Primary Series", "Kavya Rao", "Advanced", today.plusDays(4), 6, 0, 90, 8),
                slot("Evening Gentle Yoga", "Asha Menon", "Beginner", today.plusDays(5), 18, 30, 60, 12),
                slot("Private Alignment Clinic", "Meera Iyer", "All levels", today.plusDays(6), 11, 0, 45, 1)
        ));
    }

    private static YogaSlot slot(String title, String instructor, String level, LocalDate day,
                                 int hour, int minute, int duration, int capacity) {
        return new YogaSlot(title, instructor, level,
                LocalDateTime.of(day, LocalTime.of(hour, minute)), duration, capacity);
    }
}
