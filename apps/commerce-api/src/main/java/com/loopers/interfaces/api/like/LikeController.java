package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AuthenticatedUser;
import com.loopers.interfaces.auth.CurrentUser;
import com.loopers.interfaces.auth.LoginRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LikeController {

    private final LikeFacade likeFacade;

    @LoginRequired
    @PostMapping("/api/v1/products/{productId}/likes")
    public ApiResponse<Void> like(@PathVariable Long productId,
                                  @CurrentUser AuthenticatedUser user) {

        likeFacade.like(user.id(), productId);
        return ApiResponse.success(null);
    }
}
