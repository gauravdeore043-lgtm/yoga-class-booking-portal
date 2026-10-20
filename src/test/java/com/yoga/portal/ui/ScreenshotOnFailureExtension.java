package com.yoga.portal.ui;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Failure screenshot mechanism: when a Selenium test fails, the current browser window is
 * saved to target/screenshots/&lt;TestClass&gt;_&lt;method&gt;_&lt;timestamp&gt;.png together with
 * the page URL and HTML source, so the failure can be diagnosed from the Jenkins artifacts.
 */
public class ScreenshotOnFailureExtension implements TestWatcher {

    static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        WebDriver driver = BaseSeleniumTest.currentDriver();
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }
        String name = context.getRequiredTestClass().getSimpleName() + "_"
                + context.getRequiredTestMethod().getName() + "_" + LocalDateTime.now().format(STAMP);
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            Path png = SCREENSHOT_DIR.resolve(name + ".png");
            Files.copy(camera.getScreenshotAs(OutputType.FILE).toPath(), png, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(SCREENSHOT_DIR.resolve(name + ".html"),
                    "<!-- URL: " + driver.getCurrentUrl() + " -->\n<!-- Failure: " + cause + " -->\n"
                            + driver.getPageSource());
            System.out.println("[selenium] Failure screenshot saved: " + png.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.err.println("[selenium] Could not save failure screenshot: " + e);
        }
    }
}
