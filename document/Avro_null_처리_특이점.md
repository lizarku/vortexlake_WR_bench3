# Avro null 처리 특이점

## 현상
Avro 인코딩 시 `BaseLog` 필드에 null 값이 있으면 아래 에러 발생:
```
org.apache.avro.file.DataFileWriter$AppendWriteException:
  java.lang.NullPointerException: null in string in field connectIp
```

## 원인
Avro는 **스키마 기반 직렬화** 방식으로, 데이터 무결성을 강제하는 설계 철학을 가지고 있다.
기본 스키마에서 `string` 타입은 null을 허용하지 않는다.

### 인코딩 방식별 null 허용 여부

| 인코딩 | null 허용 | 이유 |
|---|---|---|
| **Kryo** | O | Java 객체를 그대로 직렬화하므로 null 자연 처리 |
| **Parquet** | O | 컬럼 기반 구조라 값이 없으면 비워둠 |
| **Tar** | O | 원본 JSON 텍스트를 그대로 묶으므로 무관 |
| **Avro** | **X (기본)** | 스키마에 명시적으로 null 허용을 선언해야 함 |

## null이 발생하는 이유
`BaseLog`는 모든 로그 타입(SecurityLog, IdsLog, SmsLog 등)의 **합집합 필드**를 정의한 모델이다.
로그 타입에 따라 특정 필드가 JSON에 존재하지 않을 수 있으며, 이 경우 Java에서 해당 필드는 null이 된다.

예시:
- SecurityLog → `connect_ip` 있음
- 다른 로그 타입 → `connect_ip` 없음 → null

## 해결 방법

### 방법 1: 데이터 전처리 (현재 적용)
`LogReader`에서 파싱 후 null String 필드를 빈 문자열(`""`)로 채운다.
```java
private static void fillNulls(BaseLog log) {
    for (Field field : BaseLog.class.getDeclaredFields()) {
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
```
- 장점: 모든 인코더가 동일한 데이터를 처리하므로 벤치마크 비교에 공정
- 장점: Avro 스키마를 수정할 필요 없음

### 방법 2: Avro 스키마에서 null 허용 (union 타입)
```json
// 기본 (null 불가)
{"name": "connectIp", "type": "string"}

// null 허용 (union 타입)
{"name": "connectIp", "type": ["null", "string"], "default": null}
```
- 장점: 데이터 원본을 변형하지 않음
- 단점: 스키마가 복잡해지고, 직렬화/역직렬화 시 오버헤드 발생

## 적용한 조치
`LogReader.java`에서 JSON 파싱 후 `fillNulls()` 메서드를 호출하여,
`BaseLog`의 모든 String 필드 중 null인 필드를 **빈 문자열(`""`)로 대체**한다.

```
JSON 파싱 → BaseLog (null 있음) → fillNulls() → BaseLog (null 없음, "" 로 대체) → 인코더(Avro/Kryo/Parquet/Tar)
```

- Avro 스키마 자체를 수정한 것이 아니라, **데이터가 Avro에 전달되기 전에 null을 제거**하는 방식
- 이 처리는 Avro뿐만 아니라 모든 인코더에 공통 적용됨

## 벤치마크에서의 판단
벤치마크 목적에서는 **방법 1(데이터 전처리)**이 적합하다.
모든 인코더가 동일한 데이터(null 없음)를 처리하므로 순수 인코딩 성능을 공정하게 비교할 수 있다.
