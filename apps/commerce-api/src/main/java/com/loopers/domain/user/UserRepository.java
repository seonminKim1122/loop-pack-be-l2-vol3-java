package com.loopers.domain.user;

import com.loopers.domain.user.vo.LoginId;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByLoginId(LoginId loginId);

    void save(User user);

    boolean existsById(Long id);

    Optional<User> findById(Long id);
}
