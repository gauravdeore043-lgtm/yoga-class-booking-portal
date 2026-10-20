package com.yoga.portal.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 2 – anyone can see the timetable with seats left. */
class SlotAvailabilitySeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("J2: slot list shows every seeded class with instructor, time and seats left")
    void slotListShowsAvailability() {
        open("/logout");
        open("/slots");

        assertThat(text(By.id("slots-title"))).isEqualTo("Classes this week");
        List<WebElement> rows = driver().findElements(By.cssSelector("tr.slot-row"));
        assertThat(rows).hasSizeGreaterThanOrEqualTo(6);

        WebElement first = rows.get(0);
        assertThat(first.findElement(By.cssSelector(".slot-title")).getText()).isNotBlank();
        assertThat(first.findElement(By.cssSelector(".slot-instructor")).getText()).isNotBlank();
        assertThat(first.findElement(By.cssSelector(".slot-time")).getText()).isNotBlank();
        int seats = Integer.parseInt(first.findElement(By.cssSelector(".seats-left")).getText().trim());
        assertThat(seats).isBetween(0, 50);

        assertThat(text(By.id("slots-signin-hint"))).contains("Register or sign in");
    }
}
