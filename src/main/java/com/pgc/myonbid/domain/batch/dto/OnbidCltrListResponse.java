package com.pgc.myonbid.domain.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.pgc.myonbid.domain.announcement.Announcement;
import com.pgc.myonbid.domain.history.AuctionHistory;
import com.pgc.myonbid.domain.item.Item;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnbidCltrListResponse {

    @JacksonXmlProperty(localName = "body")
    private Body body;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JacksonXmlProperty(localName = "items")
        private Items items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false) // <item> 태그가 리스트임
        private List<ItemDto> itemList = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemDto {

        // 1. 물건 정보
        @JacksonXmlProperty(localName = "CLTR_NO")
        private String cltrNo;      // 물건번호

        @JacksonXmlProperty(localName = "CLTR_NM")
        private String cltrNm;      // 물건명

        @JacksonXmlProperty(localName = "CTGR_FULL_NM")
        private String ctgrFullNm;  // 용도명

        @JacksonXmlProperty(localName = "LDNM_ADRS")
        private String ldnmAdrs;    // 소재지(지번)

        @JacksonXmlProperty(localName = "APSL_ASES_AVG_AMT")
        private Long apslAsesAvgAmt; // 감정가

        // 2. 공고 및 공매 이력 정보
        @JacksonXmlProperty(localName = "PLNM_NO")
        private String plnmNo;      // 공고번호

        @JacksonXmlProperty(localName = "PBCT_NO")
        private String pbctNo;      // 공매번호

        @JacksonXmlProperty(localName = "PBCT_CDTN_NO")
        private String pbctCdtnNo;  // 공매조건번호 (이것도 가끔 씀)

        @JacksonXmlProperty(localName = "MIN_BID_PRC")
        private Long minBidPrc;     // 최저입찰가

        @JacksonXmlProperty(localName = "FEE_RATE")
        private String feeRateStr;  // 최저입찰가율 (문자열 "(80%)" 등으로 옴 -> 변환 필요)

        @JacksonXmlProperty(localName = "PBCT_CLTR_STAT_NM")
        private String pbctCltrStatNm; // 물건상태

        @JacksonXmlProperty(localName = "USCBD_CNT")
        private Integer uscbdCnt;   // 유찰횟수

        @JacksonXmlProperty(localName = "DPSL_MTD_NM")
        private String dpslMtdNm;   // 처분방식 (매각/임대)

        @JacksonXmlProperty(localName = "PBCT_BEGN_DTM")
        private String pbctBegnDtm; // 입찰시작일시 (문자열로 옴)

        @JacksonXmlProperty(localName = "PBCT_CLS_DTM")
        private String pbctClsDtm;  // 입찰마감일시

        @JacksonXmlProperty(localName = "CLTR_HSTR_NO")
        private String cltrHstrNo;

        // [추가] 지도 및 상세 정보
        @JacksonXmlProperty(localName = "LDNM_PNU")
        private String ldnmPnu;

        @JacksonXmlProperty(localName = "GOODS_NM")
        private String goodsNm;

        // [추가] 입찰 방식 및 인기도
        @JacksonXmlProperty(localName = "BID_MTD_NM")
        private String bidMtdNm;

        @JacksonXmlProperty(localName = "IQRY_CNT")
        private Integer iqryCnt;

        // --- [Entity 변환 메서드] ---

        // DTO -> Item Entity 변환
        public Item toItemEntity() {
            return Item.builder()
                    .cltrNo(this.cltrNo)
                    .cltrNm(this.cltrNm)
                    .ctgrFullNm(this.ctgrFullNm)
                    .ldnmAdrs(this.ldnmAdrs)
                    .apslAsesAvgAmt(this.apslAsesAvgAmt)
                    .ldnmPnu(this.ldnmPnu)
                    .goodsNm(this.goodsNm)
                    .build();
        }

        // DTO -> AuctionHistory Entity 변환 (부모 엔티티 필요)
        public AuctionHistory toHistoryEntity(Item item, Announcement announcement) {
            BigDecimal feeRate = null;
            if (this.feeRateStr != null) {
                String rate = this.feeRateStr.replaceAll("[^0-9.]", "");
                if (!rate.isEmpty()) feeRate = new BigDecimal(rate);
            }

            // ▼▼▼ [추가] 날짜 파싱 로직 (yyyyMMddHHmmss -> LocalDateTime) ▼▼▼
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime begnDtm = null;
            LocalDateTime clsDtm = null;

            try {
                if (this.pbctBegnDtm != null && !this.pbctBegnDtm.isEmpty()) {
                    begnDtm = LocalDateTime.parse(this.pbctBegnDtm, formatter);
                }
                if (this.pbctClsDtm != null && !this.pbctClsDtm.isEmpty()) {
                    clsDtm = LocalDateTime.parse(this.pbctClsDtm, formatter);
                }
            } catch (Exception e) {
                // 날짜 형식이 이상하면 일단 null로 둠 (배치 중단 방지)
            }
            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

            return AuctionHistory.builder()
                    .item(item)
                    .announcement(announcement)
                    .pbctNo(this.pbctNo)
                    .minBidPrc(this.minBidPrc)
                    .feeRate(feeRate)
                    .pbctBegnDtm(begnDtm) // 추가
                    .pbctClsDtm(clsDtm)   // 추가
                    .pbctCltrStatNm(this.pbctCltrStatNm)
                    .uscbdCnt(this.uscbdCnt)
                    .pbctCdtnNo(this.pbctCdtnNo) // 추가
                    .cltrHstrNo(this.cltrHstrNo) // 추가
                    .bidMtdNm(this.bidMtdNm)
                    .iqryCnt(this.iqryCnt)
                    .build();
        }
    }
}
