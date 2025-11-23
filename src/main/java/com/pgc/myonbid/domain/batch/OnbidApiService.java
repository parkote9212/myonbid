package com.pgc.myonbid.domain.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.pgc.myonbid.domain.batch.dto.OnbidCltrListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnbidApiService {

    private final WebClient onbidWebClient;
    private final XmlMapper xmlMapper; // Config에 등록한 그 녀석입니다.

    @Value("${onbid.api.service-key}")
    private String serviceKey;

    public OnbidCltrListResponse getCltrList(int pageNo, int numOfRows) {
        log.info("Fetching Item List - Page: {}, Size: {}", pageNo, numOfRows);

        try {
            // 1. 재시도 로직과 함께 API 호출
            String xmlString = onbidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/KamcoPblsalThingInquireSvc/getKamcoPbctCltrList")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("DPSL_MTD_CD", "0001")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .maxBackoff(Duration.ofSeconds(10))
                            .doBeforeRetry(retrySignal -> 
                                    log.warn("API 호출 재시도 중... 시도 횟수: {}", retrySignal.totalRetries() + 1)))
                    .block();

            if (xmlString == null || xmlString.isEmpty()) {
                return null;
            }

            // 2. 받은 문자열을 우리가 만든 DTO로 수동 변환합니다.
            // 이 방식은 Jackson2XmlDecoder 의존성 문제, Content-Type 문제 모두 무시하고 작동합니다.
            return xmlMapper.readValue(xmlString, OnbidCltrListResponse.class);

        } catch (JsonProcessingException e) {
            log.error("XML Parsing Error: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            // 에러의 전체 족보(Stack Trace)를 찍어라!
            log.error("상세 에러 로그 확인:", e);
            return null;
        }
    }
}