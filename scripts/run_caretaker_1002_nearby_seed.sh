#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"
SQL_FILE="${ROOT_DIR}/scripts/seed_caretaker_1002_nearby.sql"

DB_NAME="${MYSQL_DATABASE:-pets_db}"
DB_USER="${MYSQL_USER:-root}"
DB_HOST="${MYSQL_HOST:-127.0.0.1}"
DB_PORT="${MYSQL_PORT:-3306}"
DB_PASS=""

if [[ -f "${ENV_FILE}" ]]; then
  while IFS='=' read -r key value; do
    case "${key}" in
      SPRING_DATASOURCE_PASSWORD) DB_PASS="${value}" ;;
      MYSQL_DATABASE) DB_NAME="${value}" ;;
    esac
  done < <(grep -E '^(SPRING_DATASOURCE_PASSWORD|MYSQL_DATABASE)=' "${ENV_FILE}" | tr -d '\r')
fi

if [[ -z "${DB_PASS}" ]]; then
  echo "未找到数据库密码" >&2
  exit 1
fi

mysql --default-character-set=utf8mb4 \
  -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASS}" \
  "${DB_NAME}" < "${SQL_FILE}"

cat <<'EOF'

已写入宠托师 13800000002 近距离测试数据。

打卡测试（首页「正在履约中」）：
  9301  待履约   从节点 1 开始（地址距常驻地 ~50m）
  9302  履约中   节点 1 已完成，可继续上传照片/视频

接单大厅（悬赏单，距离很近）：
  9310  喂猫
  9311  遛狗

登录：13800000002 / test1234
服务日期为今天；跨日请重新执行本脚本。

EOF
