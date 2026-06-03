package com.wotos.wotosplayerservice.repo;

import com.wotos.wotosplayerservice.dao.PlayerSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerSnapshotsRepository extends JpaRepository<PlayerSnapshot, Integer> {

    Optional<List<PlayerSnapshot>> findByAccountId(Integer accountId);

    /**
     * Returns an account's snapshots as a time series, oldest first, so callers receive a
     * chronologically ordered ("time-bucketed") history rather than insertion order.
     */
    List<PlayerSnapshot> findByAccountIdOrderByCreateTimestampAsc(Integer accountId);

    /**
     * Returns an account's snapshots whose {@code createTimestamp} falls within the inclusive
     * {@code [from, to]} epoch-second window, oldest first.
     */
    List<PlayerSnapshot> findByAccountIdAndCreateTimestampBetweenOrderByCreateTimestampAsc(
            Integer accountId, Long from, Long to);

    /**
     * Returns the most recent snapshot for an account, or empty if none exist.
     */
    Optional<PlayerSnapshot> findFirstByAccountIdOrderByCreateTimestampDesc(Integer accountId);

}
