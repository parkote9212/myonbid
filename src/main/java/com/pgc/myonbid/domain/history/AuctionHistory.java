package com.pgc.myonbid.domain.history;

import com.pgc.myonbid.domain.announcement.Announcement;
import com.pgc.myonbid.domain.common.BaseTimeEntity;
import com.pgc.myonbid.domain.item.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_auction_history")
public class AuctionHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HISTORY_ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CLTR_NO", nullable = false)
    private Item cltrNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PLNM_NO", nullable = false)
    private Announcement plnmNo;

    @Size(max = 20)
    @NotNull
    @Column(name = "PBCT_NO", nullable = false, length = 20)
    private String pbctNo;

    @Column(name = "PBCT_SEQ")
    private Integer pbctSeq;

    @Column(name = "PBCT_DGR")
    private Integer pbctDgr;

    @Column(name = "MIN_BID_PRC")
    private Long minBidPrc;

    @Column(name = "FEE_RATE", precision = 5, scale = 2)
    private BigDecimal feeRate;

    @Column(name = "PBCT_BEGN_DTM")
    private Instant pbctBegnDtm;

    @Column(name = "PBCT_CLS_DTM")
    private Instant pbctClsDtm;

    @Size(max = 50)
    @Column(name = "PBCT_CLTR_STAT_NM", length = 50)
    private String pbctCltrStatNm;

    @ColumnDefault("current_timestamp()")
    @Column(name = "REG_DT")
    private Instant regDt;

}