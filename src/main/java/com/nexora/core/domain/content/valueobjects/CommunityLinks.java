package com.nexora.core.domain.content.valueobjects;

import lombok.Getter;

@Getter
public class CommunityLinks {
    private final String whatsapp;
    private final String telegram;
    private final String discord;

    private CommunityLinks(String whatsapp, String telegram, String discord) {
        this.whatsapp = whatsapp;
        this.telegram = telegram;
        this.discord = discord;
    }

    public static CommunityLinks of(String whatsapp, String telegram, String discord) {
        return new CommunityLinks(whatsapp, telegram, discord);
    }
}
