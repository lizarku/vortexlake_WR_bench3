package com.vortexlake.bench;

import java.io.InputStream;

/**
 * 인코더의 결과를 담는 레코드.
 * 파일 단위 스트림 처리 시 읽기+파싱 시간과 직렬화 시간을 분리 측정하기 위해 사용.
 */
public record EncodeResult(
        InputStream stream,
        long parseNs,
        long serializeNs,
        int logCount
) {}
