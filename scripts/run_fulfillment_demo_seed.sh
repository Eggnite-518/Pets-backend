#!/usr/bin/env bash
# 写入履约打卡演示订单（9201/9202），需本地 MySQL 与 pets_db 已初始化
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"
SQL_FILE="${ROOT_DIR}/scripts/seed_fulfillment_demo_orders.sql"

DB_NAME="${MYSQL_DATABASE:-pets_db}"
DB_USER="${MYSQL_USER:-root}"
DB_HOST="${MYSQL_HOST:-127.0.0.1}"
DB_PORT="${MYSQL_PORT:-3306}"

if [[ -f "${ENV_FILE}" ]]; then
  while IFS='=' read -r key value; do
    case "${key}" in
      SPRING_DATASOURCE_PASSWORD) DB_PASS="${value}" ;;
      MYSQL_DATABASE) DB_NAME="${value}" ;;
    esac
  done < <(grep -E '^(SPRING_DATASOURCE_PASSWORD|MYSQL_DATABASE)=' "${ENV_FILE}" | tr -d '\r')
else
  DB_PASS="${MYSQL_ROOT_PASSWORD:-}"
fi

if [[ -z "${DB_PASS}" ]]; then
  echo "未找到数据库密码，请设置 SPRING_DATASOURCE_PASSWORD 或 MYSQL_ROOT_PASSWORD" >&2
  exit 1
fi

mysql --default-character-set=utf8mb4 \
  -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASS}" \
  "${DB_NAME}" < "${SQL_FILE}"

cat <<'EOF'

演示数据已写入。

宠托师登录：13800000002 / 密码与项目默认测试账号一致
宠主登录：  13800000001

App 操作：
  1. 宠托师端首页 →「正在履约中」应看到订单 9201、9202
  2. 进入订单详情 → 点击「进入打卡」
     - 9201：从节点 1 抵达签到开始
     - 9202：节点 1 已完成，从节点 2 入户确认（需拍照）继续
  3. 节点 6 锁门离场需上传视频，且订单须为 status=4（9202 已满足）

注意：service_date 为今天；若跨日测试请重新执行本脚本。

EOF
