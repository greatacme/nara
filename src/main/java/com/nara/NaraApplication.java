package com.nara;

import com.nara.dto.BidNoticeDto;
import com.nara.service.BidService;
import com.nara.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class NaraApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NaraApplication.class);

    @Value("#{'${nara.search.keyword}'.split(',')}")
    private List<String> keywordList;

    @Value("${nara.search.days}")
    private int days;

    @Value("#{'${nara.search.recipient}'.split(',')}")
    private List<String> recipientList;

    private final BidService bidService;
    private final EmailService emailService;

    public NaraApplication(BidService bidService, EmailService emailService) {
        this.bidService = bidService;
        this.emailService = emailService;
    }

    public static void main(String[] args) {
        SpringApplication.run(NaraApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 공백 제거
        List<String> keywords = keywordList.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        List<String> recipients = recipientList.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        logger.info("=== 나라장터 입찰공고 조회 배치 프로그램 시작 ===");
        logger.info("검색 키워드: {}", keywords);
        logger.info("조회 기간: 최근 {} 일", days);
        logger.info("수신 이메일: {}", recipients);

        try {
            // 여러 키워드에 대해 검색하고 결과 합치기
            List<BidNoticeDto> allResults = new ArrayList<>();
            for (String keyword : keywords) {
                logger.info("키워드 '{}' 검색 중...", keyword);
                List<BidNoticeDto> results = bidService.searchBidNotices(keyword, days);
                logger.info("키워드 '{}' 검색 결과: {} 건", keyword, results.size());
                allResults.addAll(results);
            }

            // 중복 제거 (bidNtceNo 기준)
            List<BidNoticeDto> uniqueResults = allResults.stream()
                    .collect(Collectors.toMap(
                            BidNoticeDto::getBidNtceNo,
                            bid -> bid,
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .sorted((a, b) -> {
                        String dateA = a.getBidNtceDt() != null ? a.getBidNtceDt() : "";
                        String dateB = b.getBidNtceDt() != null ? b.getBidNtceDt() : "";
                        return dateB.compareTo(dateA);
                    })
                    .toList();

            logger.info("전체 검색 결과 (중복 제거): {} 건", uniqueResults.size());

            // 여러 수신자에게 이메일 전송
            String keywordSummary = String.join(", ", keywords);
            for (String recipient : recipients) {
                emailService.sendBidNoticeEmail(recipient, keywordSummary, uniqueResults);
            }

            logger.info("=== 배치 프로그램 완료 ===");
        } catch (Exception e) {
            logger.error("배치 프로그램 실행 중 오류 발생", e);
            System.exit(1);
        }

        System.exit(0);
    }
}
