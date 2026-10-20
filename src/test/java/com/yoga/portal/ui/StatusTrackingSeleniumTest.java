package com.yoga.portal.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 5 – anyone can track a booking's status by reference. */
class StatusTrackingSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("J5a: tracking a valid reference shows its current status")
    void trackExistingBooking() {
        TestMember member = registerNewMember("Rahul");
        String reference = bookFirstAvailableSlot()[1];

        open("/logout");
        clickAndWait(By.id("nav-status"));
        waitFor(By.id("ref")).sendKeys(reference.toLowerCase());
        clickAndWait(By.id("track-btn"));

        assertThat(text(By.id("status-ref"))).isEqualTo(reference);
        assertThat(text(By.id("status-value"))).isEqualTo("REQUESTED");
        assertThat(driver().findElement(By.id("status-result")).getText()).contains(member.name());
    }

    @Test
    @DisplayName("J5b: an unknown reference shows 'No booking found'")
    void unknownReference() {
        open("/status?ref=YB-ZZZZZZ");
        assertThat(text(By.id("status-not-found"))).contains("No booking found");
    }
}
