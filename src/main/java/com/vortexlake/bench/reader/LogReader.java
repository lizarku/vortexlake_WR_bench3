package com.vortexlake.bench.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexlake.bench.model.RegularLog;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public class LogReader {
    public static Stream<RegularLog> read(Path doneFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return Files.lines(doneFile)
                .filter(line -> !line.isBlank())
                .map(line -> {
                    try {
                        // BOM 또는 앞부분 non-JSON prefix 제거
                        int idx = line.indexOf('{');
                        if (idx > 0) line = line.substring(idx);
                        RegularLog log = mapper.readValue(line, RegularLog.class);
                        fillNulls(log);
                        return log;
                    } catch (Exception e) {
                        System.err.println("[LogReader] 파싱 실패: " + line);
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    private static void fillNulls(RegularLog log) {
        for (Field field : RegularLog.class.getDeclaredFields()) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                try {
                    if (field.get(log) == null) {
                        field.set(log, "");
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }
    }
}
