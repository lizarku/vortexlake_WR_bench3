package com.vortexlake.bench;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class main {
    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        System.out.println("인코딩 방식을 선택하세요: ");
        System.out.println("인코딩 방식을 선택하세요:");
        System.out.println("1. tar  2. kryo  3. parquet  4. avro");
        System.out.print("> ");
        String input = scanner.nextLine().trim();
        String encodingType = switch (input){
            case "1" -> "tar";
            case "2" -> "kryo";
            case "3" -> "parquet";
            case "4" -> "avro";
            default -> throw new IllegalArgumentException("잘못된 입력" + input);
        };


        Path dirPath = Path.of("C:/logs");
        List<Path> files = Files.list(dirPath)
                .filter(p -> p.toString().endsWith(".done"))
                .toList();
        warmup(files,dirPath,encodingType);

        System.out.println("디렉토리: " + dirPath.toAbsolutePath());
        System.out.println("파일 수: " + files.size());
        files.forEach(System.out::println);
    }

    private static void benchmark(List<Path> files, Path dirPath,  String encodingType) throws Exception {
        long startTime = System.currentTimeMillis();
        long originalSize = files.stream().mapToLong(p -> p.toFile().length()).sum();
        InputStream compressed = null;
        Path outputPath =  null;

        if(encodingType.equals("kryo")){
            outputPath = Path.of("C:\\logs\\result\\" + startTime + "_kryo.zst");
            InputStream is = KryoMain.encode(files);
            compressed = PressMain.compress(is,"kryo");
        } else if (encodingType.equals("parquet")) {
            outputPath = Path.of("C:\\logs\\result\\" + startTime + "_parquet.zst");
            InputStream is = ParquetMain.encode(files);
            compressed = PressMain.compress(is,"parquet");
        }else if (encodingType.equals("avro")) {
            outputPath = Path.of("C:\\logs\\result\\" + startTime + "_avro.zst");
            InputStream is = AvroMain.encode(files);
            compressed = PressMain.compress(is,"avro");
        }else if (encodingType.equals("tar")) {
            outputPath = Path.of("C:\\logs\\result\\" + startTime + "_tar.zst");
            InputStream is = TarMain.encode(files);
            compressed = PressMain.compress(is,"tar");
        }else{

        }
        EndMain.save(compressed,outputPath,startTime, dirPath,  originalSize,"kryo");
    }

    private static void warmup(List<Path> files, Path dirPath, String encodingType) throws Exception {

        int warmupCount = Math.min(10, files.size());
        for (Path file : files) {
            if(encodingType.equals("kryo")){
                InputStream is = KryoMain.encode(List.of(file));
                PressMain.compress(is, "kryo").readAllBytes();
            } else if (encodingType.equals("parquet")) {
                InputStream is = ParquetMain.encode(List.of(file));
                PressMain.compress(is, "parquet").readAllBytes();
            }else if (encodingType.equals("avro")) {
                InputStream is = AvroMain.encode(List.of(file));
                PressMain.compress(is, "avro").readAllBytes();
            }else if (encodingType.equals("tar")) {
                InputStream is = TarMain.encode(List.of(file));
                PressMain.compress(is, "tar").readAllBytes();
            }
            warmupCount++;
            if (warmupCount >= 10) {
                System.out.println("웜업 완료");
                break;
            }
        }
        benchmark(files, dirPath, encodingType);
    }
}
