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
 *
 * PressMain 에서 받은 압축 스트림을 output 파일로 저장하고,
 * 처리 통계(시작/완료 시간, 용량, 압축률)를 리포트 파일에 기록한다.
 *
 * 리포트 파일 경로:
 *   {output 파일 위치}/report/{output 파일명}.txt
 *   예) output/20260311/sample.kryo.zst
 *    → output/20260311/report/sample.kryo.zst.txt
 */
public class EndMain {

    /**
     * 압축된 스트림을 파일로 저장하고 리포트를 기록한다.
     *
     * @param compressedStream PressMain 에서 반환된 압축 스트림
     * @param outputFilePath   저장할 output 파일 전체 경로
     * @param startTime        Main 에서 기록한 시작 시간 (epoch millis)
     * @param inputFilePath    원본 .done 파일 경로 (리포트용)
     * @param inputSize        원본 파일 용량 bytes (리포트용)
     * @param encodingType     사용된 인코딩 방식 (리포트용)
     */
    public static void save(
            java.io.InputStream compressedStream,
            java.nio.file.Path outputFilePath,
            long startTime,
            java.nio.file.Path inputFilePath,
            long inputSize,
            String encodingType) throws Exception {

        // ── STEP 1. output 파일 저장 ─────────────────────────────────────────
         FileOutputStream fos = new FileOutputStream(outputFilePath.toFile());
         compressedStream.transferTo(fos);
         fos.close();

        // ── STEP 2. 완료 시간 / output 용량 측정 ─────────────────────────────
         long endTime = System.currentTimeMillis();
         long outputSize = Files.size(outputFilePath);
         long elapsedMs = endTime - startTime;

        // ── STEP 3. report 디렉토리 준비 ─────────────────────────────────────
         Path reportDir = outputFilePath.getParent().resolve("report");
         if (!Files.exists(reportDir)) Files.createDirectories(reportDir);

        // ── STEP 4. 리포트 파일 경로 결정 ────────────────────────────────────
         String reportFileName = outputFilePath.getFileName().toString() + ".txt.txt";
         Path reportFilePath = reportDir.resolve(reportFileName);

        // ── STEP 5. 압축률 계산 ───────────────────────────────────────────────
         double compressionRatio = (1.0 - (double) outputSize / inputSize) * 100;
         String ratioStr = "%.1f%%".formatted(compressionRatio);

        // ── STEP 6. 리포트 파일 작성 ─────────────────────────────────────────
         BufferedWriter writer = Files.newBufferedWriter(reportFilePath, StandardCharsets.UTF_8);
//         아래 형식으로 작성:

//           인코딩 방식  : {encodingType}
//           원본 파일    : {inputFilePath.toAbsolutePath()}
//           원본 용량    : {inputSize} bytes ({inputSize / 1024 / 1024} MB)
//           시작 시간    : {LocalDateTime from startTime}  (yyyy-MM-dd HH:mm:ss.SSS)
//           완료 시간    : {LocalDateTime from endTime}    (yyyy-MM-dd HH:mm:ss.SSS)
//           소요 시간    : {elapsedMs} ms
//           output 파일  : {outputFilePath.toAbsolutePath()}
//           output 용량  : {outputSize} bytes ({outputSize / 1024 / 1024} MB)
//           압축률       : {compressionRatio}%
        writer.write("인코딩 방식  : " + encodingType); writer.newLine();
        writer.write("원본 파일    : " + inputFilePath.toAbsolutePath()); writer.newLine();
        writer.write("원본 용량    : " + inputSize + " bytes (" + inputSize / 1024 / 1024 + " MB)"); writer.newLine();
        writer.write("시작 시간    : " + LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime),
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))); writer.newLine();
        writer.write("완료 시간    : " + LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime),
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))); writer.newLine();
        writer.write("소요 시간    : " + elapsedMs + " ms"); writer.newLine();
        writer.write("output 파일  : " + outputFilePath.toAbsolutePath()); writer.newLine();
        writer.write("output 용량  : " + outputSize + " bytes (" + outputSize / 1024 / 1024 + " MB)"); writer.newLine();
        writer.write("압축률       : " + String.format("%.1f", compressionRatio) + "%"); writer.newLine();
         writer.close();

        // ── STEP 7. 콘솔 출력 (진행 상황 확인용) ──────────────────────────────
         System.out.printf("[완료] %s → %s (%.1f%%, %d ms)%n",
             inputFilePath.getFileName(), outputFilePath.getFileName(),
             compressionRatio, elapsedMs);
    }
}
