package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeProductInfo;

public class LikeDto {

    public record LikeProductResponse(String name, String description, String brand, long likeCount) {
        public static LikeProductResponse from(LikeProductInfo likeProductInfo) {
            return new LikeProductResponse(likeProductInfo.name(),
                                           likeProductInfo.description(),
                                           likeProductInfo.brand(),
                                           likeProductInfo.likeCount());
        }
    }
}
