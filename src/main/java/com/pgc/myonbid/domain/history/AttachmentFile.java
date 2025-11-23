package com.pgc.myonbid.domain.history;

import com.pgc.myonbid.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_ATTACHMENT_FILE")
public class AttachmentFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FILE_ID")
    private Long fileId;

    // FK 연결 (물리적으론 HISTORY_ID가 PK지만, 논리적으로 PBCT_NO로 연결됨을 가정)
    // Batch 편의성을 위해 여기선 String으로 매핑하거나, 필요시 AuctionHistory와 연결
    @Column(name = "PBCT_NO", nullable = false, length = 20)
    private String pbctNo;

    @Column(name = "ATCH_FILE_NM")
    private String atchFileNm;

    @Column(name = "ATCH_FILE_PTCS_NO", length = 50)
    private String atchFilePtcsNo;

    @Builder
    public AttachmentFile(String pbctNo, String atchFileNm, String atchFilePtcsNo) {
        this.pbctNo = pbctNo;
        this.atchFileNm = atchFileNm;
        this.atchFilePtcsNo = atchFilePtcsNo;
    }
}