package com.loopers.support.auth;

import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthAspect {

    private final UserService userService;

    @Around("@annotation(com.loopers.support.auth.LoginUser)")
    public Object authenticateUser(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String loginId = request.getHeader("X-Loopers-LoginId");
        String password = request.getHeader("X-Loopers-LoginPw");

        if (loginId == null || password == null) {
            throw new CoreException(ErrorType.UNAUTHORIZED);
        }

        userService.authenticate(loginId, password);

        return joinPoint.proceed();
    }

}
