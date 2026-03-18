package com.vortexlake.bench.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class BaseLog {
    private String eqpIp;
    private String eqpDt;
    private String recvTime;
    private String agentIp;
    private String logType;
    private String normalizeResult;
    private String dataType;
    private String key;
    private String connectIp;
    private String eqpDtMonth;
    private String eqpDtHour;
    private String eqpDtYear;
    private String eqpDtDate;
    private String eqpDtHolidayYn;
    private String inOut;
    private String eqpDtDayOfMonth;
    private String eqpDtTime;
    private String genType;
    private String eqpDtDayOfWeek;
    private String userId;
}
