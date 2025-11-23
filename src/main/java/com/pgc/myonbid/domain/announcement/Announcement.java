package com.pgc.myonbid.domain.announcement;

import com.pgc.myonbid.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
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

    @Builder
    public Announcement(String plnmNo, String plnmNm, String orgNm, String rsbyDept, String plnmDoc) {
        this.plnmNo = plnmNo;
        this.plnmNm = plnmNm;
        this.orgNm = orgNm;
        this.rsbyDept = rsbyDept;
        this.plnmDoc = plnmDoc;
    }

    public void updateDetails( String orgNm, String rsbyDept, String plnmDoc){
        this.orgNm = orgNm;
        this.rsbyDept = rsbyDept;
        this.plnmDoc = plnmDoc;
    }






}