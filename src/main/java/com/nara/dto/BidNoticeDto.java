package com.nara.dto;

import lombok.Data;

@Data
public class BidNoticeDto {
    private String bidNtceNo;        // 입찰공고번호
    private String bidNtceNm;        // 입찰공고명
    private String bidNtceDt;        // 입찰공고일시 (게시일시)
    private String ntceInsttNm;      // 공고기관명
    private String dminsttNm;        // 수요기관명
    private String bidBeginDt;       // 입찰시작일시
    private String bidClseDt;        // 입찰마감일시
    private String opengDt;          // 개찰일시
    private String rbidPermsnYn;     // 재입찰허용여부
    private String totPrdprc;        // 총예정가격
    private String bidNtceUrl;       // 입찰공고URL
}
