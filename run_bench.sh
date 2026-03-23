#!/bin/bash
export JAVA_HOME=/VortexLake/app/java/zulu21/jdk-21.0.2
export PATH=$JAVA_HOME/bin:$PATH

JAR="/opt/vortexlake_WR_bench3/target/benchmarks.jar"
ROUNDS=10

# 1=tar, 2=kryo, 3=parquet, 4=avro
for TYPE in 1 2 3 4; do
    case $TYPE in
        1) NAME="tar" ;;
        2) NAME="kryo" ;;
        3) NAME="parquet" ;;
        4) NAME="avro" ;;
    esac

    echo "===== $NAME 벤치마크 시작 ($ROUNDS회) ====="
    for i in $(seq 1 $ROUNDS); do
        echo "--- $NAME [$i/$ROUNDS] ---"
        echo "$TYPE" | java --enable-preview -cp "$JAR" com.vortexlake.bench.main
        # 디스크 절약: .zst 결과 파일 삭제 (리포트 txt는 유지)
        rm -f /opt/logs/result/*.zst
        rm -f /tmp/tar_* /tmp/kryo_* /tmp/avro_* /tmp/zstd_* 2>/dev/null
        find /tmp -maxdepth 1 -name "parquet_*" -type f -delete 2>/dev/null
        echo "--- $NAME [$i/$ROUNDS] 완료 ---"
        echo ""
    done
    echo "===== $NAME 벤치마크 종료 ====="
    echo ""
done

echo "전체 벤치마크 완료!"
