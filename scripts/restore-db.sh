#!/bin/sh
set -eu

DUMP_FILE="${1:-}"
CONFIRM="${CONFIRM_RESTORE:-}"

if [ -z "${DUMP_FILE}" ]; then
  echo "Uso: CONFIRM_RESTORE=YES ./scripts/restore-db.sh /backups/arquivo.dump"
  exit 1
fi

if [ "${CONFIRM}" != "YES" ]; then
  echo "Defina CONFIRM_RESTORE=YES para confirmar a restauração destrutiva do banco."
  exit 1
fi

echo "Restaurando ${DUMP_FILE} em ${POSTGRES_DB}..."
pg_restore \
  -h "${POSTGRES_HOST:-santos-chamados-db}" \
  -p "${POSTGRES_PORT:-5432}" \
  -U "${POSTGRES_USER}" \
  -d "${POSTGRES_DB}" \
  --clean \
  --if-exists \
  "${DUMP_FILE}"

echo "Restauração concluída. Reinicie a aplicação e valide a integridade."
