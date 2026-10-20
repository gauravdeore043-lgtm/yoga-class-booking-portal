package com.yoga.portal.ui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Starts the whole application on a random port and drives it with Chrome.
 * Headless by default; run with -Dselenium.headless=false to watch the browser.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseSeleniumTest {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static WebDriver driver;

    @LocalServerPort
    private int port;

    static WebDriver currentDriver() {
        return driver;
    }

    @BeforeAll
    static void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("selenium.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900", "--no-sandbox", "--disable-dev-shm-usage",
                "--disable-gpu", "--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void stopBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // ---------- helpers ----------

    protected WebDriver driver() {
        return driver;
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected void open(String path) {
        driver.get(url(path));
    }

    protected WebElement waitFor(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Clicks an element that navigates and waits until the old page is gone. */
    protected void clickAndWait(By locator) {
        clickAndWait(waitFor(locator));
    }

    protected void clickAndWait(WebElement element) {
        element.click();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.stalenessOf(element));
    }

    protected String text(By locator) {
        return waitFor(locator).getText().trim();
    }

    /** Test data: a unique member per test so tests never depend on each other. */
    protected record TestMember(String name, String email, String phone) {
    }

    protected TestMember uniqueMember(String firstName) {
        int n = COUNTER.incrementAndGet();
        return new TestMember(firstName + " Tester" + n,
                firstName.toLowerCase() + "." + System.currentTimeMillis() + "." + n + "@example.com",
                "98765" + String.format("%05d", n));
    }

    /** Signs out (if needed) and registers a new member through the UI. */
    protected TestMember registerNewMember(String firstName) {
        open("/logout");
        TestMember member = uniqueMember(firstName);
        open("/register");
        waitFor(By.id("name")).sendKeys(member.name());
        driver.findElement(By.id("email")).sendKeys(member.email());
        driver.findElement(By.id("phone")).sendKeys(member.phone());
        clickAndWait(By.id("register-submit"));
        waitFor(By.id("flash-success"));
        return member;
    }

    /** The first slot row on /slots whose Book button is enabled. */
    protected WebElement firstBookableSlot() {
        open("/slots");
        waitFor(By.id("slots-table"));
        List<WebElement> rows = driver.findElements(By.cssSelector("tr.slot-row"));
        return rows.stream()
                .filter(r -> r.findElement(By.cssSelector(".book-btn")).isEnabled())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No bookable slot found on /slots"));
    }

    protected int seatsLeft(String slotId) {
        open("/slots");
        return Integer.parseInt(text(By.cssSelector("#slot-" + slotId + " .seats-left")));
    }

    /** Books the first available slot and returns {slotId, reference}. */
    protected String[] bookFirstAvailableSlot() {
        WebElement row = firstBookableSlot();
        String slotId = row.getAttribute("data-slot-id");
        clickAndWait(row.findElement(By.cssSelector(".book-btn")));
        String reference = text(By.id("booking-ref"));
        return new String[]{slotId, reference};
    }
}
