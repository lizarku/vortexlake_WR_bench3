package com.vortexlake.bench;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.vortexlake.bench.model.RegularLog;
import com.vortexlake.bench.reader.LogReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class KryoMain {

    public static EncodeResult encode(List<Path> files) throws Exception {
        Kryo kryo = new Kryo();
        kryo.register(RegularLog.class);
        Path tempFile = Files.createTempFile("kryo_", ".kryo");
        Output output = new Output(new BufferedOutputStream(new FileOutputStream(tempFile.toFile())));

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
                kryo.writeObject(output, log);
            }
            long s2 = System.nanoTime();
            totalSerializeNs += (s2 - s1);

            totalCount += logs.size();
        }
        output.close();
        System.out.println("count: " + totalCount);

        InputStream stream = new BufferedInputStream(new FileInputStream(tempFile.toFile()) {
            @Override
            public void close() throws IOException {
                super.close();
                Files.deleteIfExists(tempFile);
            }
        });
        return new EncodeResult(stream, totalParseNs, totalSerializeNs, totalCount);
    }
}
