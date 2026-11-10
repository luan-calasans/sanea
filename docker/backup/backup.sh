#!/bin/sh
set -eu

TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
BACKUP_DIR="/backups"
TMP_FILE="${BACKUP_DIR}/.tmp_${TIMESTAMP}.dump"
OUT_FILE="${BACKUP_DIR}/santos_chamados_${TIMESTAMP}.dump"
LOG_FILE="${BACKUP_DIR}/backup.log"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"

mkdir -p "${BACKUP_DIR}"

log() {
  echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) $*" | tee -a "${LOG_FILE}"
}

log "Iniciando backup PostgreSQL"

if ! pg_dump \
  -h "${POSTGRES_HOST}" \
  -p "${POSTGRES_PORT}" \
  -U "${POSTGRES_USER}" \
  -d "${POSTGRES_DB}" \
  -Fc \
  -f "${TMP_FILE}"; then
  log "ERRO: pg_dump falhou; backups anteriores preservados"
  rm -f "${TMP_FILE}"
  exit 1
fi

if [ ! -s "${TMP_FILE}" ]; then
  log "ERRO: arquivo de backup vazio; backups anteriores preservados"
  rm -f "${TMP_FILE}"
  exit 1
fi

mv "${TMP_FILE}" "${OUT_FILE}"
log "Backup concluido: ${OUT_FILE}"

find "${BACKUP_DIR}" -type f -name 'santos_chamados_*.dump' -mtime "+${RETENTION_DAYS}" -print -delete \
  | while read -r old; do
      log "Backup expirado removido: ${old}"
    done

log "Retencao aplicada (${RETENTION_DAYS} dias)"
