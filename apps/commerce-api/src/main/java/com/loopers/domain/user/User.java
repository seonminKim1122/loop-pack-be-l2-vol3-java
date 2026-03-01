package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Embedded
    private LoginId loginId;

    @Column(name = "password")
    private String encodedPassword;

    @Embedded
    private Name name;

    @Embedded
    private BirthDate birthDate;

    @Embedded
    private Email email;

    protected User() {}

    private User(LoginId loginId, String encodedPassword, Name name, BirthDate birthDate, Email email) {
        this.loginId = loginId;
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.birthDate = birthDate;
        this.email = email;
    }

    public static User create(LoginId loginId, String encodedPassword, Name name, BirthDate birthDate, Email email) {
        return new User(loginId, encodedPassword, name, birthDate, email);
    }

    public LoginId loginId() {
        return loginId;
    }

    public String password() {
        return encodedPassword;
    }

    public Name name() {
        return name;
    }

    public BirthDate birthDate() {
        return birthDate;
    }

    public Email email() {
        return email;
    }

    public void changePassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }
}
