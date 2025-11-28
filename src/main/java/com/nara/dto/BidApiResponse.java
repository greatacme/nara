package com.nara.dto;

import lombok.Data;
import java.util.List;

@Data
public class BidApiResponse {
    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {
        private List<BidNoticeDto> items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }
}
