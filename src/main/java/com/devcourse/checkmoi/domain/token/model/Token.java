package com.devcourse.checkmoi.domain.token.model;

import com.devcourse.checkmoi.global.model.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String refreshToken;

    @Column(nullable = false, unique = true)
    private long userId;

    public Token(String refreshToken, Long userId) {
        this(null, refreshToken, userId);
    }

    @Builder
    public Token(Long id, String refreshToken, Long userId) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }

    public void refresh(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
