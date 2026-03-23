package com.vortexlake.bench;

import com.vortexlake.bench.model.RegularLog;
import com.vortexlake.bench.reader.LogReader;
import org.apache.avro.reflect.ReflectData;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ParquetMain {

    public static EncodeResult encode(List<Path> files) throws Exception {
        Path tempFile = Path.of(System.getProperty("java.io.tmpdir"), "parquet_" + UUID.randomUUID() + ".parquet");

        org.apache.hadoop.fs.Path hadoopPath =
                new org.apache.hadoop.fs.Path(tempFile.toUri());

        long totalParseNs = 0;
        long totalSerializeNs = 0;
        int totalCount = 0;

        try (ParquetWriter<RegularLog> writer =
                AvroParquetWriter.<RegularLog>builder(hadoopPath)
                        .withSchema(AvroMain.SCHEMA)
                        .withDataModel(ReflectData.get())
                        .withCompressionCodec(CompressionCodecName.UNCOMPRESSED)
                        .withConf(new Configuration())
                        .build()) {

            for (Path file : files) {
                long p1 = System.nanoTime();
                List<RegularLog> logs = LogReader.read(file).collect(Collectors.toList());
                long p2 = System.nanoTime();
                totalParseNs += (p2 - p1);

                long s1 = System.nanoTime();
                for (RegularLog log : logs) {
                    writer.write(log);
                }
                long s2 = System.nanoTime();
                totalSerializeNs += (s2 - s1);

                totalCount += logs.size();
            }
        }

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
