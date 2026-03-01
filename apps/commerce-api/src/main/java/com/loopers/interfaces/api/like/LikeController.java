package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.LikeProductInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AuthenticatedUser;
import com.loopers.interfaces.auth.CurrentUser;
import com.loopers.interfaces.auth.LoginRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @LoginRequired
    @DeleteMapping("/api/v1/products/{productId}/likes")
    public ApiResponse<Void> unlike(@PathVariable Long productId,
                                    @CurrentUser AuthenticatedUser user) {
        likeFacade.unlike(user.id(), productId);
        return ApiResponse.success(null);
    }

    @LoginRequired
    @GetMapping("/api/v1/users/likes")
    public ApiResponse<List<LikeDto.LikeProductResponse>> getLikeList(@CurrentUser AuthenticatedUser user) {
        List<LikeProductInfo> likeProductInfos = likeFacade.getLikeList(user.id());
        List<LikeDto.LikeProductResponse> response = likeProductInfos.stream().map(LikeDto.LikeProductResponse::from).toList();
        return ApiResponse.success(response);
    }
}
