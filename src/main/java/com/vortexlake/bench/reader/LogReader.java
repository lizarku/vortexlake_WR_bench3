package com.vortexlake.bench.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexlake.bench.model.BaseLog;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LogReader {
    public static Stream<BaseLog> read(Path doneFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return Files.lines(doneFile)
                .filter(line -> !line.isBlank())
                .map(line -> {
                    try {
                        return mapper.readValue(line, BaseLog.class);
                    } catch (Exception e) {
                        System.err.println("[LogReader] 파싱 실패: " + line);
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }
}
