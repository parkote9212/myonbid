package com.pgc.myonbid.domain.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnbidAnnouncementDetailResponse {

    @JacksonXmlProperty(localName = "body")
    private Body body;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JacksonXmlProperty(localName = "item")
        private DetailItem item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailItem {
        @JacksonXmlProperty(localName = "PLNM_NM")
        private String plnmNm; // 공고명

        @JacksonXmlProperty(localName = "ORG_NM")
        private String orgNm; // 공고기관

        @JacksonXmlProperty(localName = "RSBY_DEPT")
        private String rsbyDept; // 담당부점

        @JacksonXmlProperty(localName = "PSCG_NM")
        private String pscgNm; // 담당자명

        @JacksonXmlProperty(localName = "PSCG_TPNO")
        private String pscgTpno; // 담당자전화번호

        @JacksonXmlProperty(localName = "PLNM_DOC")
        private String plnmDoc; // ★ 핵심: 공고문 HTML


    }
}