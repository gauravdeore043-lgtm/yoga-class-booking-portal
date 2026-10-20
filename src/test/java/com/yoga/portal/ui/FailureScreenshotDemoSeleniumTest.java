package com.yoga.portal.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates the failure-screenshot mechanism. Skipped in normal runs; enable with
 * {@code mvn test -Dtest=FailureScreenshotDemoSeleniumTest -Ddemo.failure=true}.
 * It fails on purpose and leaves a PNG in target/screenshots/.
 */
@EnabledIfSystemProperty(named = "demo.failure", matches = "true")
class FailureScreenshotDemoSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("DEMO: deliberately failing assertion to show the screenshot-on-failure mechanism")
    void deliberatelyFails() {
        open("/");
        assertThat(text(By.id("home-title"))).isEqualTo("This title does not exist");
    }
}
