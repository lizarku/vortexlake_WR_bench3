package com.vortexlake.bench;

import com.vortexlake.bench.model.RegularLog;
import com.vortexlake.bench.reader.LogReader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class main {

    private static final long HEAP_SIZE_MB = 4096; // 힙 메모리 크기 (MB)

    public static void main(String[] args) throws Exception {

        // 현재 힙이 설정값과 다르면 원하는 힙 크기로 재실행
        long currentMaxHeapMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        if (currentMaxHeapMB < HEAP_SIZE_MB - 100 || currentMaxHeapMB > HEAP_SIZE_MB + 100) {
            System.out.println("현재 힙: " + currentMaxHeapMB + "MB → " + HEAP_SIZE_MB + "MB 로 재실행합니다.");
            relaunchWithHeap(args);
            return;
        }
        System.out.println("힙 메모리: " + currentMaxHeapMB + "MB (고정)");

        Scanner scanner = new Scanner(System.in);
        System.out.println("인코딩 방식을 선택하세요:");
        System.out.println("1. tar  2. kryo  3. parquet  4. avro");
        System.out.print("> ");
        String input = scanner.nextLine().trim();
        String encodingType = switch (input){
            case "1" -> "tar";
            case "2" -> "kryo";
            case "3" -> "parquet";
            case "4" -> "avro";
            default -> throw new IllegalArgumentException("잘못된 입력: " + input);
        };

        Path dirPath = Path.of("/opt/logs/");
        List<Path> files = Files.list(dirPath)
                .filter(p -> p.toString().endsWith(".done"))
                .toList();
        System.out.println("디렉토리: " + dirPath.toAbsolutePath());
        System.out.println("파일 수: " + files.size());

        Thread loader = startLoading("처리 중");
        try {
            warmup(files, encodingType);
            benchmark(files, dirPath, encodingType);
        } finally {
            stopLoading(loader);
        }
        System.out.println("완료!");
    }

    private static void benchmark(List<Path> files, Path dirPath, String encodingType) throws Exception {
        long originalSize = files.stream().mapToLong(p -> p.toFile().length()).sum();

        // ── STEP 1+2. 읽기+파싱+직렬화 (파일 단위 스트림 처리) ───────────
        long t1 = System.nanoTime();
        EncodeResult result;
        if (encodingType.equals("kryo")) {
            result = KryoMain.encode(files);
        } else if (encodingType.equals("parquet")) {
            result = ParquetMain.encode(files);
        } else if (encodingType.equals("avro")) {
            result = AvroMain.encode(files);
        } else {
            result = TarMain.encode(files);
        }
        long t2 = System.nanoTime();
        System.out.printf("[단계1] 읽기+파싱: %,d건, %,d ms%n", result.logCount(), result.parseNs() / 1_000_000);
        System.out.printf("[단계2] 직렬화(%s): %,d ms%n", encodingType, result.serializeNs() / 1_000_000);

        // ── STEP 3. 압축 (CountingInputStream으로 직렬화 크기 측정) ───────
        long t3 = System.nanoTime();
        CountingInputStream countingEncoded = new CountingInputStream(result.stream());
        InputStream compressed = PressMain.compress(countingEncoded, encodingType);
        long serializedSize = countingEncoded.getBytesRead();
        long t4 = System.nanoTime();
        long compressNs = t4 - t3;
        System.out.printf("[단계3] 압축(zstd): %,d ms (직렬화 크기: %,d bytes, %d MB)%n",
                compressNs / 1_000_000, serializedSize, serializedSize / 1024 / 1024);

        // ── STEP 4. 저장 ──────────────────────────────────────────────────
        Path resultDir = Path.of("/opt/logs/result");
        if (!Files.exists(resultDir)) Files.createDirectories(resultDir);
        Path outputPath = resultDir.resolve(t1 + "_" + encodingType + ".zst");

        EndMain.save(compressed, outputPath, t1, dirPath, originalSize, encodingType,
                result.logCount(), result.parseNs(), result.serializeNs(), compressNs, serializedSize);
        long t5 = System.nanoTime();
        long saveNs = t5 - t4;
        System.out.printf("[단계4] 저장: %,d ms%n", saveNs / 1_000_000);
        System.out.printf("[합계] 전체 소요: %,d ms%n", (t5 - t1) / 1_000_000);
    }

    private static void warmup(List<Path> files, String encodingType) throws Exception {
        int warmupCount = 0;
        for (Path file : files) {
            List<RegularLog> logs = LogReader.read(file).collect(Collectors.toList());
            if (encodingType.equals("kryo")) {
                PressMain.compress(com.vortexlake.bench.KryoMain.encode(List.of(file)).stream(), "kryo").readAllBytes();
            } else if (encodingType.equals("parquet")) {
                PressMain.compress(com.vortexlake.bench.ParquetMain.encode(List.of(file)).stream(), "parquet").readAllBytes();
            } else if (encodingType.equals("avro")) {
                PressMain.compress(com.vortexlake.bench.AvroMain.encode(List.of(file)).stream(), "avro").readAllBytes();
            } else {
                PressMain.compress(com.vortexlake.bench.TarMain.encode(List.of(file)).stream(), "tar").readAllBytes();
            }
            warmupCount++;
            if (warmupCount >= 30) {
                System.out.println("웜업 완료 (" + warmupCount + "개 파일)");
                break;
            }
        }
    }

    private static final AtomicBoolean loading = new AtomicBoolean(false);

    private static Thread startLoading(String message) {
        loading.set(true);
        Thread thread = new Thread(() -> {
            String[] dots = {"", ".", "..", "..."};
            int i = 0;
            while (loading.get()) {
                System.out.print("\r" + message + dots[i % dots.length] + "   ");
                i++;
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void stopLoading(Thread thread) {
        loading.set(false);
        try { thread.join(1000); } catch (InterruptedException ignored) {}
        System.out.print("\r                              \r");
    }

    private static void relaunchWithHeap(String[] args) throws Exception {
        String javaHome = ProcessHandle.current().info().command().orElse("java");
        String classpath = System.getProperty("java.class.path");

        List<String> command = new ArrayList<>();
        command.add(javaHome);
        command.add("-Xms" + HEAP_SIZE_MB + "m");
        command.add("-Xmx" + HEAP_SIZE_MB + "m");
        command.add("--enable-preview");
        command.add("-cp");
        command.add(classpath);
        command.add(main.class.getName());
        for (String arg : args) command.add(arg);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        System.exit(process.waitFor());
    }
}
