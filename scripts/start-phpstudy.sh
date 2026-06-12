#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# 优先使用当前项目旁边下载好的 Maven，其次使用系统 PATH。
if [[ -x "$ROOT_DIR/../tools/apache-maven-3.9.9/bin/mvn" ]]; then
  export PATH="$ROOT_DIR/../tools/apache-maven-3.9.9/bin:$PATH"
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "未找到 Maven，请先安装 Maven 或确认 ../tools/apache-maven-3.9.9 已存在。"
  exit 1
fi

if ! nc -z localhost 3306 >/dev/null 2>&1; then
  echo "未检测到 localhost:3306 的 MySQL。"
  echo "请先在 phpStudy/小皮面板里启动 MySQL，并确认账号 root、密码 root。"
  exit 1
fi

echo ">>> 检测到 MySQL 已启动，开始运行 Spring Boot..."
mvn spring-boot:run
