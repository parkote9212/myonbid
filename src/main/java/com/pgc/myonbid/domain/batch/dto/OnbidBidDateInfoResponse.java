package com.pgc.myonbid.domain.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnbidBidDateInfoResponse {

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
        @JacksonXmlProperty(localName = "bidDateInfoItem")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<BidDateDto> dateList = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BidDateDto {
        @JacksonXmlProperty(localName = "PBCT_NO")
        private String pbctNo;

        @JacksonXmlProperty(localName = "PBCT_SEQ")
        private Integer pbctSeq; // 회차

        @JacksonXmlProperty(localName = "PBCT_DGR")
        private Integer pbctDgr; // 차수

        @JacksonXmlProperty(localName = "TDPS_RT")
        private Integer tdpsRt;  // 보증금률

        @JacksonXmlProperty(localName = "BID_DVSN_NM")
        private String bidDvsnNm; // 입찰구분(인터넷/현장)

        @JacksonXmlProperty(localName = "PBCT_BEGN_DTM")
        private String pbctBegnDtm; // 시작일시

        @JacksonXmlProperty(localName = "PBCT_CLS_DTM")
        private String pbctClsDtm;  // 마감일시

        @JacksonXmlProperty(localName = "PBCT_EXCT_DTM")
        private String pbctExctDtm; // 개찰일시
    }
}