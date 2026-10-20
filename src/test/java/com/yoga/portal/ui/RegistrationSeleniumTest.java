package com.yoga.portal.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 1 – a visitor registers and is signed in. */
class RegistrationSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("J1a: new visitor registers and is welcomed by name")
    void visitorCanRegister() {
        TestMember member = registerNewMember("Ananya");

        assertThat(driver().getCurrentUrl()).endsWith("/slots");
        assertThat(text(By.id("flash-success"))).contains("Welcome, " + member.name());
        assertThat(text(By.id("nav-member"))).isEqualTo(member.name());
    }

    @Test
    @DisplayName("J1b: registering twice with the same email shows an error")
    void duplicateEmailIsRejected() {
        TestMember member = registerNewMember("Vikram");
        open("/logout");

        open("/register");
        waitFor(By.id("name")).sendKeys("Someone Else");
        driver().findElement(By.id("email")).sendKeys(member.email());
        driver().findElement(By.id("phone")).sendKeys("9123456789");
        clickAndWait(By.id("register-submit"));

        assertThat(text(By.id("flash-error"))).contains("already exists");
    }

    @Test
    @DisplayName("J1c: invalid email and phone show field errors")
    void invalidInputShowsFieldErrors() {
        open("/logout");
        open("/register");
        waitFor(By.id("name")).sendKeys("Bad Input");
        driver().findElement(By.id("email")).sendKeys("not-an-email");
        driver().findElement(By.id("phone")).sendKeys("12");
        clickAndWait(By.id("register-submit"));

        assertThat(text(By.id("email-error"))).contains("valid email");
        assertThat(text(By.id("phone-error"))).contains("7-15 digits");
    }
}
