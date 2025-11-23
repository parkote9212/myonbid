package com.pgc.myonbid.domain.history;

import com.pgc.myonbid.domain.announcement.Announcement;
import com.pgc.myonbid.domain.common.BaseTimeEntity;
import com.pgc.myonbid.domain.item.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_auction_history")
public class AuctionHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HISTORY_ID", nullable = false)
    private Long historyId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CLTR_NO", nullable = false)
    private Item item; //물건정보

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PLNM_NO", nullable = false)
    private Announcement announcement; //공고정보

    @Size(max = 20)
    @NotNull
    @Column(name = "PBCT_NO", nullable = false, length = 20)
    private String pbctNo; // 공매번호

    @Column(name = "PBCT_SEQ")
    private Integer pbctSeq; // 회차

    @Column(name = "PBCT_DGR")
    private Integer pbctDgr; // 차수

    @Column(name = "MIN_BID_PRC")
    private Long minBidPrc; //최저입찰가

    @Column(name = "FEE_RATE", precision = 5, scale = 2)
    private BigDecimal feeRate; // 최저 입찰가율

    @Column(name = "TDPS_RT")
    private Integer tdpsRt; // 보증금률

    @Column(name = "PBCT_BEGN_DTM")
    private LocalDateTime pbctBegnDtm; // 입찰시작

    @Column(name = "PBCT_CLS_DTM")
    private LocalDateTime pbctClsDtm; // 입찰마감

    @Column(name = "PBCT_EXCT_DTM")
    private LocalDateTime pbctExctDtm; // 개찰일시

    @Column(name = "PBCT_CLTR_STAT_NM", length = 50)
    private String pbctCltrStatNm; // 상태 (유찰 등)

    @Column(name = "USCBD_CNT")
    private Integer uscbdCnt; // 유찰횟수

    @Column(name = "BID_MTD_NM", length = 100)
    private String bidMtdNm;

    @Column(name = "IQRY_CNT")
    private Integer iqryCnt;

    @Column(name = "PBCT_CDTN_NO", length = 20)
    private String pbctCdtnNo;

    @Column(name = "CLTR_HSTR_NO", length = 20)
    private String cltrHstrNo;

    @Builder
    public AuctionHistory(Item item, Announcement announcement, String pbctNo, Integer pbctSeq, Integer pbctDgr, Long minBidPrc, BigDecimal feeRate, LocalDateTime pbctBegnDtm, LocalDateTime pbctClsDtm, String pbctCltrStatNm, Integer uscbdCnt, String bidMtdNm, Integer iqryCnt, String pbctCdtnNo, String cltrHstrNo) {
        this.item = item;
        this.announcement = announcement;
        this.pbctNo = pbctNo;
        this.pbctSeq = pbctSeq;
        this.pbctDgr = pbctDgr;
        this.minBidPrc = minBidPrc;
        this.feeRate = feeRate;
        this.pbctBegnDtm = pbctBegnDtm;
        this.pbctClsDtm = pbctClsDtm;
        this.pbctCltrStatNm = pbctCltrStatNm;
        this.uscbdCnt = uscbdCnt;
        this.bidMtdNm = bidMtdNm;
        this.iqryCnt = iqryCnt;
        this.pbctCdtnNo = pbctCdtnNo;
        this.cltrHstrNo = cltrHstrNo;
    }

    public void updateSchedule(LocalDateTime begn, LocalDateTime cls, LocalDateTime exct, Integer tdpsRt) {
        this.pbctBegnDtm = begn;
        this.pbctClsDtm = cls;
        this.pbctExctDtm = exct;
        this.tdpsRt = tdpsRt;
    }



}