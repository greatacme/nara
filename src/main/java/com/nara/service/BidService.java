package com.nara.service;

import com.nara.dto.BidApiResponse;
import com.nara.dto.BidNoticeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BidService {

    private static final Logger logger = LoggerFactory.getLogger(BidService.class);
    private final RestTemplate restTemplate;

    @Value("${nara.api.url}")
    private String apiUrl;

    @Value("${nara.api.key}")
    private String apiKey;

    public BidService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<BidNoticeDto> searchBidNotices(String keyword, int days) {
        try {
            // 오늘 날짜와 N일 전 날짜 계산 (YYYYMMDDHHmm 형식)
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(days);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            String inqryBgnDt = startDate.format(formatter) + "0000";
            String inqryEndDt = today.format(formatter) + "2359";

            List<BidNoticeDto> allResults = new ArrayList<>();

            // 나라장터 검색조건(PPS)을 사용한 엔드포인트
            String[] endpoints = {
                    "/getBidPblancListInfoThngPPSSrch",    // 물품
                    "/getBidPblancListInfoServcPPSSrch",   // 용역
                    "/getBidPblancListInfoCnstwkPPSSrch",  // 공사
                    "/getBidPblancListInfoFrgcptPPSSrch"   // 외자
            };

            for (String endpoint : endpoints) {
                try {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl + endpoint)
                            .queryParam("serviceKey", apiKey)
                            .queryParam("type", "json")
                            .queryParam("numOfRows", "999")
                            .queryParam("pageNo", "1")
                            .queryParam("inqryDiv", "1")  // 1: 입찰공고일시
                            .queryParam("inqryBgnDt", inqryBgnDt)
                            .queryParam("inqryEndDt", inqryEndDt);

                    // 공고명 검색 조건 추가
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        builder.queryParam("bidNtceNm", keyword);
                    }

                    String url = builder.build(false).toUriString();

                    ResponseEntity<BidApiResponse> response = restTemplate.getForEntity(url, BidApiResponse.class);

                    if (response.getBody() != null &&
                        response.getBody().getResponse() != null &&
                        response.getBody().getResponse().getBody() != null &&
                        response.getBody().getResponse().getBody().getItems() != null) {
                        allResults.addAll(response.getBody().getResponse().getBody().getItems());
                    }
                } catch (Exception e) {
                    logger.error("Error calling Nara API endpoint: {}", endpoint, e);
                }
            }

            logger.info("Total items fetched with keyword '{}': {}", keyword, allResults.size());

            // 게시일시 기준으로 최신순 정렬 (내림차순)
            List<BidNoticeDto> sortedResults = allResults.stream()
                    .sorted((a, b) -> {
                        String dateA = a.getBidNtceDt() != null ? a.getBidNtceDt() : "";
                        String dateB = b.getBidNtceDt() != null ? b.getBidNtceDt() : "";
                        return dateB.compareTo(dateA); // 내림차순
                    })
                    .toList();

            // 결과 샘플 출력
            if (!sortedResults.isEmpty()) {
                logger.info("Sample results (first 5):");
                sortedResults.stream().limit(5).forEach(item ->
                    logger.info("  - {} (게시일시: {})", item.getBidNtceNm(), item.getBidNtceDt())
                );
            }

            return sortedResults;
        } catch (Exception e) {
            logger.error("Error in searchBidNotices", e);
            return new ArrayList<>();
        }
    }
}
