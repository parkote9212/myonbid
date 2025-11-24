package com.pgc.myonbid.domain.history;

import com.pgc.myonbid.domain.announcement.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {

    // [Batch용] 공매번호(PBCT_NO)로 이미 존재하는지 확인 (중복 수집 방지)
    boolean existsByPbctNo(String pbctNo);

    // [Batch용] 공매번호로 데이터 찾기 (일정 업데이트할 때 사용)
    Optional<AuctionHistory> findByPbctNo(String pbctNo);

    // [Service용] 특정 물건(CLTR_NO)의 모든 이력을 회차(SEQ)와 차수(DGR) 순서대로 조회
    // 그래프 그릴 때 이 메소드를 호출하면 됩니다.
    List<AuctionHistory> findAllByItem_CltrNoOrderByPbctSeqAscPbctDgrAsc(String cltrNo);

    @Query("SELECT h FROM AuctionHistory h WHERE h.pbctSeq IS NULL")
    List<AuctionHistory> findIncompleteHistories(org.springframework.data.domain.Pageable pageable);

    Optional<AuctionHistory> findFirstByAnnouncement(Announcement announcement);
}