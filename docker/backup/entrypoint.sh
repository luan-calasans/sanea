#!/bin/sh
set -eu

chmod +x /scripts/backup.sh || true

SCHEDULE="${BACKUP_SCHEDULE:-0 3 * * *}"
CRON_FILE="/tmp/backup-cron"

echo "${SCHEDULE} /scripts/backup.sh >> /backups/backup.log 2>&1" > "${CRON_FILE}"
crontab "${CRON_FILE}"

echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) Agendador de backup iniciado: ${SCHEDULE}" >> /backups/backup.log

/scripts/backup.sh || true

exec crond -f -l 8
