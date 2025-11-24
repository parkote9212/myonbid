package com.pgc.myonbid.domain.batch.dto;

import com.pgc.myonbid.domain.history.AttachmentFile;
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
public class OnbidFileInfoResponse {

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
        @JacksonXmlProperty(localName = "fileItem")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<FileDto> fileList = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileDto {
        @JacksonXmlProperty(localName = "ATCH_FILE_PTCS_NO")
        private String atchFilePtcsNo; // 다운로드 ID

        @JacksonXmlProperty(localName = "ATCH_FILE_NM")
        private String atchFileNm; // 파일명

        // Entity 변환
        public AttachmentFile toEntity(String pbctNo) {
            return AttachmentFile.builder()
                    .pbctNo(pbctNo)
                    .atchFileNm(this.atchFileNm)
                    .atchFilePtcsNo(this.atchFilePtcsNo)
                    .build();
        }
    }
}