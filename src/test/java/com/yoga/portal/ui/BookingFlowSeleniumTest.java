package com.yoga.portal.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 3 – register, request a booking, confirm it, see it in My Bookings. */
class BookingFlowSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("J3: member requests a booking (seat held), confirms it and sees it in My Bookings")
    void requestAndConfirmBooking() {
        registerNewMember("Priya");
        String slotId = firstBookableSlot().getAttribute("data-slot-id");
        int seatsBefore = seatsLeft(slotId);

        String[] booking = bookFirstAvailableSlotWithId(slotId);
        String reference = booking[1];

        assertThat(reference).matches("YB-[A-Z0-9]{6}");
        assertThat(text(By.id("booking-status"))).isEqualTo("REQUESTED");
        assertThat(text(By.id("flash-success"))).contains(reference);
        assertThat(seatsLeft(slotId)).isEqualTo(seatsBefore - 1);

        open("/bookings/" + reference);
        clickAndWait(By.id("confirm-btn"));
        assertThat(text(By.id("booking-status"))).isEqualTo("CONFIRMED");
        assertThat(text(By.id("flash-success"))).contains("confirmed");

        clickAndWait(By.id("nav-my-bookings"));
        List<WebElement> rows = driver().findElements(By.cssSelector("tr.my-booking-row"));
        WebElement row = rows.stream()
                .filter(r -> reference.equals(r.getAttribute("data-ref")))
                .findFirst().orElseThrow(() -> new AssertionError("Booking not listed in My Bookings"));
        assertThat(row.findElement(By.cssSelector(".status")).getText()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("J3b: booking the same class twice is blocked")
    void duplicateBookingBlocked() {
        registerNewMember("Arjun");
        String[] booking = bookFirstAvailableSlot();

        open("/slots");
        clickAndWait(By.id("book-" + booking[0]));

        assertThat(text(By.id("flash-error"))).contains("already have an active booking");
    }

    private String[] bookFirstAvailableSlotWithId(String slotId) {
        open("/slots");
        clickAndWait(By.id("book-" + slotId));
        return new String[]{slotId, text(By.id("booking-ref"))};
    }
}
