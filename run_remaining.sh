#!/bin/bash
export JAVA_HOME=/VortexLake/app/java/zulu21/jdk-21.0.2
export PATH=$JAVA_HOME/bin:$PATH

JAR="/opt/vortexlake_WR_bench3/target/benchmarks.jar"

# parquet 나머지 7라운드
echo "===== parquet 벤치마크 이어서 (7회) ====="
for i in $(seq 4 10); do
    echo "--- parquet [$i/10] ---"
    echo "3" | java --enable-preview -cp "$JAR" com.vortexlake.bench.main
    rm -f /opt/logs/result/*.zst
    rm -f /tmp/tar_* /tmp/kryo_* /tmp/avro_* /tmp/zstd_* 2>/dev/null
    find /tmp -maxdepth 1 -name "parquet_*" -type f -delete 2>/dev/null
    echo "--- parquet [$i/10] 완료 ---"
done

# avro 10라운드
echo "===== avro 벤치마크 시작 (10회) ====="
for i in $(seq 1 10); do
    echo "--- avro [$i/10] ---"
    echo "4" | java --enable-preview -cp "$JAR" com.vortexlake.bench.main
    rm -f /opt/logs/result/*.zst
    rm -f /tmp/tar_* /tmp/kryo_* /tmp/avro_* /tmp/zstd_* 2>/dev/null
    find /tmp -maxdepth 1 -name "parquet_*" -type f -delete 2>/dev/null
    echo "--- avro [$i/10] 완료 ---"
done

echo "나머지 벤치마크 완료!"
