package com.devcourse.checkmoi.domain.user.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserRole {
    LOGIN("ROLE_LOGIN");

    private final String grantedAuthority;

    public String getGrantedAuthority() {
        return grantedAuthority;
    }
}
