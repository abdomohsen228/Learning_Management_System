package com.LMS.Learning_Management_System.util;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;



public class UserSignUpRequest {

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
    @NotEmpty
    private int userTypeId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public int getUserTypeId() {
        return userTypeId;
    }
    public void setUserTypeId(int userTypeId) {
        this.userTypeId = userTypeId;
    }
}
