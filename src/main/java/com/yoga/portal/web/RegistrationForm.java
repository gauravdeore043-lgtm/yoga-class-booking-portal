package com.yoga.portal.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Please enter your name")
    @Size(max = 80, message = "Name must be at most 80 characters")
    private String name;

    @NotBlank(message = "Please enter your email")
    @Email(message = "Please enter a valid email address")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "Please enter your phone number")
    @Pattern(regexp = "^[0-9+\\- ]{7,15}$", message = "Phone must be 7-15 digits")
    private String phone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
