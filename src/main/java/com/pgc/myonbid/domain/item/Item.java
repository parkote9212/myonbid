package com.pgc.myonbid.domain.item;

import com.pgc.myonbid.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@ToString
@Table(name = "tb_item")
public class Item extends BaseTimeEntity {
    @Id
    @Size(max = 20)
    @Column(name = "CLTR_NO", nullable = false, length = 20)
    private String cltrNo; //물건번호 pk

    @Size(max = 500)
    @Column(name = "CLTR_NM", length = 500)
    private String cltrNm; // 물건명

    @Size(max = 200)
    @Column(name = "CTGR_FULL_NM", length = 200)
    private String ctgrFullNm; // 용도명

    @Size(max = 500)
    @Column(name = "LDNM_ADRS", length = 500)
    private String ldnmAdrs; // 지번부소

    @Column(name = "APSL_ASES_AVG_AMT")
    private Long apslAsesAvgAmt; // 감정가

    @Builder
    public Item(String cltrNo, String cltrNm, String ctgrFullNm, String ldnmAdrs, Long apslAsesAvgAmt) {
        this.cltrNo = cltrNo;
        this.cltrNm = cltrNm;
        this.ctgrFullNm = ctgrFullNm;
        this.ldnmAdrs = ldnmAdrs;
        this.apslAsesAvgAmt = apslAsesAvgAmt;
    }
}