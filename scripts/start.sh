#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# 优先使用项目内 Maven，其次系统 PATH
if [[ -x "$ROOT_DIR/../tools/apache-maven-3.9.9/bin/mvn" ]]; then
  export PATH="$ROOT_DIR/../tools/apache-maven-3.9.9/bin:$PATH"
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "未找到 Maven。请先安装：brew install maven"
  echo "或等待 tools/apache-maven-3.9.9 下载完成。"
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker 未运行，请先启动 Docker Desktop。"
  exit 1
fi

echo ">>> 启动 MySQL (Docker)..."
docker compose up -d

echo ">>> 等待 MySQL 就绪..."
for i in $(seq 1 60); do
  if docker exec exchange-mysql mysqladmin ping -uroot -proot --silent 2>/dev/null; then
    echo "MySQL 已就绪"
    break
  fi
  sleep 2
done

echo ">>> 编译并启动 Spring Boot..."
mvn spring-boot:run
