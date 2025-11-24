package com.pgc.myonbid.domain.announcement;

import com.pgc.myonbid.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_announcement")
@ToString
public class Announcement extends BaseTimeEntity {
    @Id
    @Size(max = 20)
    @Column(name = "PLNM_NO", nullable = false, length = 20)
    private String plnmNo; // 공고번호 pk

    @Size(max = 500)
    @NotNull
    @Column(name = "PLNM_NM", nullable = false, length = 500)
    private String plnmNm; // 공고명

    @Size(max = 100)
    @Column(name = "ORG_NM", length = 100)
    private String orgNm; //공고기관명

    @Size(max = 100)
    @Column(name = "RSBY_DEPT", length = 100)
    private String rsbyDept; // 담당부서

    @Column(name = "PLNM_DOC", columnDefinition = "LONGTEXT")
    private String plnmDoc;//공고 상세

    @Size(max = 100)
    @Column(name = "PSCG_NM", length = 100)
    private String pscgNm; // 담당자명

    @Size(max = 50)
    @Column(name = "PSCG_TPNO", length = 50)
    private String pscgTpno; // 담당자번호

    @Builder
    public Announcement(String plnmNo, String plnmNm, String orgNm, String rsbyDept, String plnmDoc) {
        this.plnmNo = plnmNo;
        this.plnmNm = plnmNm;
        this.orgNm = orgNm;
        this.rsbyDept = rsbyDept;
        this.plnmDoc = plnmDoc;
    }

    public void updateDetails(String plnmNm, String orgNm, String rsbyDept, String pscgNm, String pscgTpno, String plnmDoc) {
        this.plnmNm = plnmNm;
        this.orgNm = orgNm;
        this.rsbyDept = rsbyDept;
        this.pscgNm = pscgNm;
        this.pscgTpno = pscgTpno;
        this.plnmDoc = plnmDoc;
    }
}