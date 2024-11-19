package com.redirectUrlShortner;

public class UrlData {
    private String originalUrl;
    private Long expirationTime;

    public UrlData() {
    }

    public UrlData(final String originalUrl, final Long expirationTime) {
        this.originalUrl = originalUrl;
        this.expirationTime = expirationTime;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(final String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(final Long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
