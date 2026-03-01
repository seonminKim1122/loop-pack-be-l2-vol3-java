package com.loopers.domain.user;

import com.loopers.domain.user.vo.LoginId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    Optional<User> findByLoginId(LoginId loginId);

    void save(User user);

    boolean existsById(Long id);

    Optional<User> findById(Long id);

    List<User> findAllByIdIn(Set<Long> ids);
}
