#!/bin/bash
export JAVA_HOME=/VortexLake/app/java/zulu21/jdk-21.0.2
export PATH=$JAVA_HOME/bin:$PATH
JAR="/opt/vortexlake_WR_bench3/target/benchmarks.jar"

for TYPE in 1 2 3 4; do
    case $TYPE in
        1) NAME="tar" ;; 2) NAME="kryo" ;; 3) NAME="parquet" ;; 4) NAME="avro" ;;
    esac
    echo "===== $NAME 생성 ====="
    echo "$TYPE" | java --enable-preview -cp "$JAR" com.vortexlake.bench.main
    # temp 파일만 삭제, .zst는 유지
    rm -f /tmp/tar_* /tmp/kryo_* /tmp/avro_* /tmp/zstd_* 2>/dev/null
    find /tmp -maxdepth 1 -name "parquet_*" -type f -delete 2>/dev/null
    echo "===== $NAME 완료 ====="
done
echo "전체 완료!"
