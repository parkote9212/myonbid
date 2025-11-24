package com.pgc.myonbid.domain.announcement;

import org.springframework.data.domain.Pageable; // 주의: jpa가 아니라 domain 패키지
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {

    // 상세 정보(HTML)가 없는 공고 조회
    @Query("SELECT a FROM Announcement a WHERE a.plnmDoc IS NULL")
    List<Announcement> findIncompleteAnnouncements(Pageable pageable);
}