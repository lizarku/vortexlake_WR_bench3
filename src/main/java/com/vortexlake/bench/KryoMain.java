package com.vortexlake.bench;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexlake.bench.model.BaseLog;
import com.vortexlake.bench.reader.LogReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class KryoMain {

    public static InputStream encode(List<Path> files) throws Exception {
        int count = 0;
        Kryo kryo = new Kryo();
        kryo.register(BaseLog.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        ObjectMapper mapper = new ObjectMapper();

        for (Path file : files) {
            LogReader.read(file).forEach(log -> {
                kryo.writeObject(output, log);
//                count++;
            });
        }
        output.close();
        System.out.println("count: " + count);
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
