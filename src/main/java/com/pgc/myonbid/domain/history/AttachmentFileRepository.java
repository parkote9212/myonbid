package com.pgc.myonbid.domain.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentFileRepository extends JpaRepository<AttachmentFile, Long> {

    // 공매번호로 첨부파일 목록 조회
    List<AttachmentFile> findAllByPbctNo(String pbctNo);
}
