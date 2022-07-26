package com.devcourse.checkmoi.domain.user.dto.response;

import com.devcourse.checkmoi.domain.user.entity.User;

public record MyUserInfoResponse(String name, String email,
                                 String profileImageUrl, String role) {
    public static MyUserInfoResponse from(User user) {
        return new MyUserInfoResponse(user.getName(), user.getEmail().getValue(),
            user.getProfileImgUrl(), user.getUserRole().getGrantedAuthority());
    }
}
