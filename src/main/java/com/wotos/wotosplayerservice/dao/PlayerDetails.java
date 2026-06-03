package com.wotos.wotosplayerservice.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class PlayerDetails {

    @Id
    @Column(name = "account_id", nullable = false)
    private Integer accountId;
    @Column(name = "nickname", nullable = false)
    private String nickname;
    @Column(name = "client_language")
    private String clientLanguage;
    @Column(name = "last_battle_time")
    private Integer lastBattleTime;
    @Column(name = "created_at")
    private Integer createdAt;
    @Column(name = "updated_at")
    private Integer updatedAt;
    @Column(name = "global_rating")
    private Integer globalRating;
    @Column(name = "clan_id")
    private Integer clanId;
    @Column(name = "logout_at")
    private Integer logoutAt;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getClientLanguage() {
        return clientLanguage;
    }

    public void setClientLanguage(String clientLanguage) {
        this.clientLanguage = clientLanguage;
    }

    public Integer getLastBattleTime() {
        return lastBattleTime;
    }

    public void setLastBattleTime(Integer lastBattleTime) {
        this.lastBattleTime = lastBattleTime;
    }

    public Integer getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Integer updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getGlobalRating() {
        return globalRating;
    }

    public void setGlobalRating(Integer globalRating) {
        this.globalRating = globalRating;
    }

    public Integer getClanId() {
        return clanId;
    }

    public void setClanId(Integer clanId) {
        this.clanId = clanId;
    }

    public Integer getLogoutAt() {
        return logoutAt;
    }

    public void setLogoutAt(Integer logoutAt) {
        this.logoutAt = logoutAt;
    }
}
