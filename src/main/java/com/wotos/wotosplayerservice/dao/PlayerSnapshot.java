package com.wotos.wotosplayerservice.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_snapshots")
public class PlayerSnapshot {

    @Id
    @Column(name = "player_snapshot_id")
    private Integer playerSnapshotId;
    @Column(name = "account_id", nullable = false)
    private Integer accountId;
    @Column(name = "create_timestamp", nullable = false)
    private Long createTimestamp;
    @Column(name = "nickname", nullable = false)
    private String nickname;
    @Column(name = "global_rating", nullable = false)
    private Integer globalRating;
    @Column(name = "clan_id")
    private Integer clanId;

    public Integer getPlayerSnapshotId() {
        return playerSnapshotId;
    }

    public void setPlayerSnapshotId(Integer playerSnapshotId) {
        this.playerSnapshotId = playerSnapshotId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
}
