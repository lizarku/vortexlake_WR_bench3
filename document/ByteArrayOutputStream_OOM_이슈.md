# ByteArrayOutputStream OutOfMemoryError 이슈 및 해결

## 발생일
2026-03-18

## 현상
```
Exception in thread "main" java.lang.OutOfMemoryError: Required array length 2147483639 + 9 is too large
    at java.base/jdk.internal.util.ArraysSupport.hugeLength(ArraysSupport.java:752)
    at java.base/java.io.ByteArrayOutputStream.ensureCapacity(ByteArrayOutputStream.java:100)
```
- 로컬(Windows) 환경에서는 정상 동작했으나, 서버 환경에서 670개 파일(총 3.3GB) 처리 시 OOM 발생

## 원인
`ByteArrayOutputStream`은 내부적으로 `byte[]` 배열을 사용한다.
Java 배열의 최대 크기는 약 **2,147,483,647 (2GB)** 이므로, 3.3GB 데이터를 메모리에 올리면 배열 한계를 초과한다.

### 문제 코드 (변경 전)
```java
// TarMain, KryoMain, AvroMain, PressMain 모두 동일한 패턴
ByteArrayOutputStream baos = new ByteArrayOutputStream();
// ... 데이터를 baos에 write ...
return new ByteArrayInputStream(baos.toByteArray());  // 전체 데이터를 메모리에 복제
```

**추가 문제점:**
- `baos.toByteArray()`는 내부 배열을 **복사**하므로, 실제로는 데이터 크기의 **2배** 메모리가 필요
- 예: 3.3GB 데이터 → baos 내부 배열 3.3GB + toByteArray() 복사본 3.3GB = **6.6GB 필요**

## 해결 방법: 임시 파일(Temp File) 방식

### 변경 후 코드
```java
Path tempFile = Files.createTempFile("tar_", ".tar");
try (FileOutputStream fos = new FileOutputStream(tempFile.toFile());
     BufferedOutputStream bos = new BufferedOutputStream(fos);
     TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos)) {
    // ... 데이터를 파일에 write ...
}
return new BufferedInputStream(new FileInputStream(tempFile.toFile()) {
    @Override
    public void close() throws IOException {
        super.close();
        Files.deleteIfExists(tempFile);  // 스트림 종료 시 임시 파일 자동 삭제
    }
});
```

### 적용 대상
| 클래스 | 임시 파일 접두사 | 설명 |
|---|---|---|
| TarMain | `tar_*.tar` | Tar 아카이브 인코딩 |
| KryoMain | `kryo_*.kryo` | Kryo 직렬화 인코딩 |
| AvroMain | `avro_*.avro` | Avro 직렬화 인코딩 |
| PressMain | `zstd_*.zst` | Zstd 압축 |

## 방식별 비교

| 방식 | 메모리 사용 | 속도 | 데이터 크기 제한 | 실무 적합도 |
|---|---|---|---|---|
| ByteArrayOutputStream | 데이터 x 2배 | 빠름 | ~2GB (배열 한계) | 소규모 데이터 |
| 임시 파일 (Temp File) | 버퍼 크기만큼 | 약간 느림 (OS 캐시로 완화) | 디스크 여유만큼 | 대용량 데이터, 벤치마크 |
| PipedStream | 파이프 버퍼만큼 | 빠름 | 무제한 | 프로덕션 스트리밍 |

## 참고사항
- 임시 파일은 OS의 페이지 캐시에 의해 상당 부분 메모리에 캐싱되므로, 순수 디스크 I/O보다 빠르게 동작
- 스트림 close() 시 임시 파일이 자동 삭제되도록 구현하여 디스크 누수 방지
- 벤치마크에서 인코딩/압축 시간을 분리 측정하려면 임시 파일 방식이 PipedStream보다 적합
