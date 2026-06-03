package com.wotos.wotosplayerservice.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_achievements_snapshots")
public class PlayerAchievementsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_achievements_snapshot_id")
    private Integer playerAchievementsSnapshotId;
    @Column(name = "account_id")
    private Integer accountId;

    public Integer getPlayerAchievementsSnapshotId() {
        return playerAchievementsSnapshotId;
    }

    public void setPlayerAchievementsSnapshotId(Integer playerAchievementsSnapshotId) {
        this.playerAchievementsSnapshotId = playerAchievementsSnapshotId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

}
