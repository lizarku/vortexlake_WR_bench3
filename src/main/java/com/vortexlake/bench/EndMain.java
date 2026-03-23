package com.vortexlake.bench;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 파일 저장 및 리포트 기록 담당 모듈.
 */
public class EndMain {

    public static void save(
            java.io.InputStream compressedStream,
            java.nio.file.Path outputFilePath,
            long startNano,
            java.nio.file.Path inputFilePath,
            long inputSize,
            String encodingType,
            int logCount,
            long parseNs,
            long serializeNs,
            long compressNs,
            long serializedSize) throws Exception {

        long saveStartNano = System.nanoTime();

        // ── STEP 1. output 파일 저장 ─────────────────────────────────────────
        FileOutputStream fos = new FileOutputStream(outputFilePath.toFile());
        compressedStream.transferTo(fos);
        fos.close();

        // ── STEP 2. 완료 시간 / output 용량 측정 ─────────────────────────────
        long endNano = System.nanoTime();
        long outputSize = Files.size(outputFilePath);
        long saveNs = endNano - saveStartNano;
        long totalNs = endNano - startNano;

        // ms 변환
        long parseMs = parseNs / 1_000_000;
        long serializeMs = serializeNs / 1_000_000;
        long compressMs = compressNs / 1_000_000;
        long saveMs = saveNs / 1_000_000;
        long totalMs = totalNs / 1_000_000;

        // ── STEP 3. 비율 계산 ────────────────────────────────────────────────
        // 원본 대비 직렬화 비율 (직렬화로 얼마나 줄었나/늘었나)
        double serializeRatio = (double) serializedSize / inputSize * 100;
        // 원본 대비 최종 압축 비율
        double totalCompressionRatio = (1.0 - (double) outputSize / inputSize) * 100;
        // 직렬화 대비 압축 비율 (압축이 직렬화 산출물을 얼마나 줄였나)
        double zstdCompressionRatio = (1.0 - (double) outputSize / serializedSize) * 100;

        // ── STEP 4. report 디렉토리 준비 ─────────────────────────────────────
        Path reportDir = Path.of("/opt/logs/result/report");
        if (!Files.exists(reportDir)) Files.createDirectories(reportDir);

        // ── STEP 5. 리포트 파일 경로 결정 ────────────────────────────────────
        String reportFileName = outputFilePath.getFileName().toString() + ".txt";
        Path reportFilePath = reportDir.resolve(reportFileName);

        // ── STEP 6. 리포트 파일 작성 ─────────────────────────────────────────
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        long now = System.currentTimeMillis();
        BufferedWriter writer = Files.newBufferedWriter(reportFilePath, StandardCharsets.UTF_8);

        writer.write("========================================"); writer.newLine();
        writer.write("  벤치마크 결과 리포트"); writer.newLine();
        writer.write("========================================"); writer.newLine();
        writer.newLine();

        writer.write("인코딩 방식    : " + encodingType); writer.newLine();
        writer.write("원본 경로      : " + inputFilePath.toAbsolutePath()); writer.newLine();
        writer.write("로그 건수      : " + String.format("%,d", logCount) + " 건"); writer.newLine();
        writer.newLine();

        writer.write("--- 시간 측정 (nanoTime 기반) ---"); writer.newLine();
        writer.write("시작 시간      : " + LocalDateTime.ofInstant(Instant.ofEpochMilli(now - totalMs),
                ZoneId.systemDefault()).format(dtf)); writer.newLine();
        writer.write("완료 시간      : " + LocalDateTime.ofInstant(Instant.ofEpochMilli(now),
                ZoneId.systemDefault()).format(dtf)); writer.newLine();
        writer.write("읽기+파싱      : " + String.format("%,d", parseMs) + " ms"); writer.newLine();
        writer.write("직렬화         : " + String.format("%,d", serializeMs) + " ms"); writer.newLine();
        writer.write("압축(zstd L1)  : " + String.format("%,d", compressMs) + " ms"); writer.newLine();
        writer.write("저장           : " + String.format("%,d", saveMs) + " ms"); writer.newLine();
        writer.write("전체 소요      : " + String.format("%,d", totalMs) + " ms"); writer.newLine();
        writer.newLine();

        writer.write("--- 용량 ---"); writer.newLine();
        writer.write("원본 용량      : " + String.format("%,d", inputSize) + " bytes ("
                + inputSize / 1024 / 1024 + " MB)"); writer.newLine();
        writer.write("직렬화 후 용량 : " + String.format("%,d", serializedSize) + " bytes ("
                + serializedSize / 1024 / 1024 + " MB)"); writer.newLine();
        writer.write("압축 후 용량   : " + String.format("%,d", outputSize) + " bytes ("
                + outputSize / 1024 / 1024 + " MB)"); writer.newLine();
        writer.newLine();

        writer.write("--- 비율 분석 ---"); writer.newLine();
        writer.write("원본 대비 직렬화 용량  : " + String.format("%.1f", serializeRatio) + "%"
                + (serializeRatio < 100 ? " (축소)" : " (확대)")); writer.newLine();
        writer.write("직렬화 대비 압축 절감  : " + String.format("%.1f", zstdCompressionRatio) + "%"); writer.newLine();
        writer.write("원본 대비 최종 절감    : " + String.format("%.1f", totalCompressionRatio) + "%"); writer.newLine();
        writer.newLine();

        writer.write("output 파일    : " + outputFilePath.toAbsolutePath()); writer.newLine();
        writer.close();

        // ── STEP 7. 콘솔 출력 ───────────────────────────────────────────────
        System.out.printf("[완료] %s | 원본: %dMB → 직렬화: %dMB (%.1f%%) → 압축: %dMB (최종 절감 %.1f%%, %,d ms)%n",
                encodingType,
                inputSize / 1024 / 1024,
                serializedSize / 1024 / 1024,
                serializeRatio,
                outputSize / 1024 / 1024,
                totalCompressionRatio,
                totalMs);
    }
}
