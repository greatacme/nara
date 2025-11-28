package com.nara.service;

import com.nara.dto.BidNoticeDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBidNoticeEmail(String recipient, String keyword, List<BidNoticeDto> results) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipient);
            helper.setSubject("[나라장터] '" + keyword + "' 검색 결과 (" + results.size() + "건)");
            helper.setText(buildEmailContent(keyword, results), true);

            mailSender.send(message);
            logger.info("이메일 전송 완료: {} ({} 건)", recipient, results.size());
        } catch (Exception e) {
            logger.warn("이메일 전송 실패 (메일 서버가 설정되지 않았거나 연결할 수 없습니다): {}", e.getMessage());
            logger.info("검색 결과는 정상적으로 조회되었습니다. 로컬 메일 서버 설정 후 재시도하세요.");
        }
    }

    private String buildEmailContent(String keyword, List<BidNoticeDto> results) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 800px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }");
        html.append(".card { border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin-bottom: 15px; background: #f9f9f9; }");
        html.append(".card-title { font-size: 1.2em; font-weight: bold; color: #333; margin-bottom: 10px; }");
        html.append(".info-row { margin: 5px 0; }");
        html.append(".label { font-weight: bold; color: #667eea; display: inline-block; min-width: 120px; }");
        html.append(".badge { background: #667eea; color: white; padding: 3px 10px; border-radius: 12px; font-size: 0.9em; }");
        html.append("a { color: #667eea; text-decoration: none; }");
        html.append("a:hover { text-decoration: underline; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        html.append("<div class='header'>");
        html.append("<h1>나라장터 입찰공고 검색 결과</h1>");
        html.append("<p>검색 키워드: <strong>").append(keyword).append("</strong></p>");
        html.append("<p>검색 결과: <strong>").append(results.size()).append("건</strong></p>");
        html.append("</div>");

        if (results.isEmpty()) {
            html.append("<p>검색 결과가 없습니다.</p>");
        } else {
            for (BidNoticeDto bid : results) {
                html.append("<div class='card'>");
                html.append("<div class='badge'>").append(bid.getBidNtceNo()).append("</div>");
                html.append("<div class='card-title'>").append(bid.getBidNtceNm()).append("</div>");

                if (bid.getBidNtceDt() != null) {
                    html.append("<div class='info-row'>");
                    html.append("<span class='label'>게시일시:</span> ").append(bid.getBidNtceDt());
                    html.append("</div>");
                }

                if (bid.getNtceInsttNm() != null) {
                    html.append("<div class='info-row'>");
                    html.append("<span class='label'>공고기관:</span> ").append(bid.getNtceInsttNm());
                    html.append("</div>");
                }

                if (bid.getBidBeginDt() != null) {
                    html.append("<div class='info-row'>");
                    html.append("<span class='label'>입찰시작일시:</span> ").append(bid.getBidBeginDt());
                    html.append("</div>");
                }

                if (bid.getBidClseDt() != null) {
                    html.append("<div class='info-row'>");
                    html.append("<span class='label'>입찰마감일시:</span> ").append(bid.getBidClseDt());
                    html.append("</div>");
                }

                if (bid.getTotPrdprc() != null) {
                    html.append("<div class='info-row'>");
                    html.append("<span class='label'>총 예정가격:</span> ").append(bid.getTotPrdprc()).append("원");
                    html.append("</div>");
                }

                if (bid.getBidNtceUrl() != null) {
                    html.append("<div class='info-row' style='margin-top: 10px;'>");
                    html.append("<a href='").append(bid.getBidNtceUrl()).append("' target='_blank'>공고 상세보기 →</a>");
                    html.append("</div>");
                }

                html.append("</div>");
            }
        }

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
