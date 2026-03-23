package com.vortexlake.bench;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexlake.bench.model.RegularLog;
import com.vortexlake.bench.reader.LogReader;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class TarMain {

    public static EncodeResult encode(List<Path> files) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1단계: 파일 단위로 읽기+파싱 → JSON 직렬화 → 임시 파일에 순차 쓰기
        Path jsonTempFile = Files.createTempFile("tar_json_", ".jsonl");
        BufferedOutputStream jsonOut = new BufferedOutputStream(new FileOutputStream(jsonTempFile.toFile()));

        long totalParseNs = 0;
        long totalSerializeNs = 0;
        int totalCount = 0;

        for (Path file : files) {
            long p1 = System.nanoTime();
            List<RegularLog> logs = LogReader.read(file).collect(Collectors.toList());
            long p2 = System.nanoTime();
            totalParseNs += (p2 - p1);

            long s1 = System.nanoTime();
            for (RegularLog log : logs) {
                jsonOut.write(mapper.writeValueAsBytes(log));
                jsonOut.write('\n');
            }
            long s2 = System.nanoTime();
            totalSerializeNs += (s2 - s1);

            totalCount += logs.size();
        }
        jsonOut.close();

        // 2단계: 임시 파일 크기로 tar entry 생성 후 순차 복사
        long s3 = System.nanoTime();
        Path tarTempFile = Files.createTempFile("tar_", ".tar");
        try (FileOutputStream fos = new FileOutputStream(tarTempFile.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos)) {
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            TarArchiveEntry entry = new TarArchiveEntry("logs.json");
            entry.setSize(Files.size(jsonTempFile));
            tarOut.putArchiveEntry(entry);
            Files.copy(jsonTempFile, tarOut);
            tarOut.closeArchiveEntry();
            tarOut.finish();
        } finally {
            Files.deleteIfExists(jsonTempFile);
        }
        long s4 = System.nanoTime();
        totalSerializeNs += (s4 - s3);

        System.out.println("count: " + totalCount);

        InputStream stream = new BufferedInputStream(new FileInputStream(tarTempFile.toFile()) {
            @Override
            public void close() throws IOException {
                super.close();
                Files.deleteIfExists(tarTempFile);
            }
        });
        return new EncodeResult(stream, totalParseNs, totalSerializeNs, totalCount);
    }
}
