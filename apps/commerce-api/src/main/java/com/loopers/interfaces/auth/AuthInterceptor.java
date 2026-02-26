package com.loopers.interfaces.auth;

import com.loopers.application.user.UserFacade;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserFacade userFacade;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        if (method.hasMethodAnnotation(AdminOnly.class)) {
            String ldap = request.getHeader("X-Loopers-Ldap");
            if (!"loopers.admin".equals(ldap)) {
                throw new CoreException(ErrorType.FORBIDDEN);
            }
            return true;
        }

        if (!method.hasMethodAnnotation(LoginRequired.class)) {
            return true;
        }

        String loginId = request.getHeader("X-Loopers-LoginId");
        String password = request.getHeader("X-Loopers-LoginPw");

        if (loginId == null || password == null) {
            throw new CoreException(ErrorType.UNAUTHORIZED);
        }

        Long userId = userFacade.authenticate(loginId, password);
        request.setAttribute("authenticatedUser", new AuthenticatedUser(userId));
        return true;
    }
}
