package com.vortexlake.bench.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class RegularLog {
    // 장비/수집 기본 정보
    private String eqpIp;
    private String eqpDt;
    private String recvTime;
    private String agentIp;
    private String connectIp;
    private String macAddr;
    private String logType;
    private String dataType;
    private String normalizeResult;
    private String key;
    private String parserKey;

    // 날짜/시간 분해 필드
    private String eqpDtYear;
    private String eqpDtMonth;
    private String eqpDtDate;
    private String eqpDtDayOfMonth;
    private String eqpDtDayOfWeek;
    private String eqpDtHour;
    private String eqpDtTime;
    private String eqpDtHolidayYn;

    // 이벤트 시간
    private String startTime;
    private String endTime;
    private String eventTime;
    private String gatherEtime;

    // 출발지 정보
    private String srcIp;
    private String srcPort;
    private String srcCountryCode;
    private String srcCountryName;
    private String srcBlackip;
    private String srcBlackipSeverity;
    private String srcAssetGroupCd;
    private String srcAssetId;
    private String srcAssetNm;

    // 목적지 정보
    private String dstnIp;
    private String dstnPort;
    private String dstnCountryCode;
    private String dstnCountryName;
    private String dstnBlackip;
    private String dstnBlackipSeverity;
    private String dstnAssetGroupCd;
    private String dstnAssetId;
    private String dstnAssetNm;

    // 프로토콜/이벤트 정보
    private String prtc;
    private String msg;
    private String attackNm;
    private String action;
    private String faultMsg;
    private String inOut;
    private String userId;

    // 기관 코드
    private String instCd1;
    private String instCd2;

    // 카운트
    private String cnt;
    private String count;

    // 원본/파싱 정보
    private String originalLog;
    private String parseFailMsg;

    // 테스트 필드
    private String test;
    @JsonProperty("Test1")
    private String test1;
}
