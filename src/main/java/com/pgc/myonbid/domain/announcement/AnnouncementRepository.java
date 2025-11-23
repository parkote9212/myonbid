package com.pgc.myonbid.domain.announcement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {

    // [Batch용] 아직 상세 정보(HTML)가 수집되지 않은 공고들의 ID만 조회
    // 공고문 내용(PLNM_DOC)이 NULL인 것만 골라내서 상세 조회 API를 쏘기 위함
    @Query("SELECT a.plnmNo FROM Announcement a WHERE a.plnmDoc IS NULL")
    List<String> findPlnmNosMissingDetails();
}