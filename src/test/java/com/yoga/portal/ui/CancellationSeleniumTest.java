package com.yoga.portal.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 4 – cancelling a booking releases the seat. */
class CancellationSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("J4: cancelling a booking sets CANCELLED and gives the seat back")
    void cancelReleasesSeat() {
        registerNewMember("Neha");
        String slotId = firstBookableSlot().getAttribute("data-slot-id");
        int seatsBefore = seatsLeft(slotId);

        open("/slots");
        clickAndWait(By.id("book-" + slotId));
        String reference = text(By.id("booking-ref"));
        assertThat(seatsLeft(slotId)).as("seat held after booking").isEqualTo(seatsBefore - 1);

        open("/bookings/" + reference);
        clickAndWait(By.id("cancel-btn"));

        assertThat(text(By.id("booking-status"))).isEqualTo("CANCELLED");
        assertThat(driver().findElements(By.id("cancel-btn"))).as("no cancel button after cancelling").isEmpty();
        assertThat(seatsLeft(slotId)).as("seat released after cancellation").isEqualTo(seatsBefore);
    }
}
