package com.wotos.wotosplayerservice.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_achievements_snapshots")
public class PlayerAchievementsSnapshot {

    @Id
    @Column(name = "player_achievements_snapshot_id")
    private Integer playerAchievementsSnapshotId;
    @Column(name = "account_id")
    private Integer accountId;

}
