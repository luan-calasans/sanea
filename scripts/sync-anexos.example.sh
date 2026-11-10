#!/bin/sh
set -eu

SOURCE="${ATTACHMENT_STORAGE_PATH:-/data/anexos}"
TARGET="${ATTACHMENT_SYNC_TARGET:-}"

if [ -z "${TARGET}" ]; then
  echo "Configure ATTACHMENT_SYNC_TARGET antes de sincronizar anexos."
  exit 1
fi

echo "Destino configurado: ${TARGET}"
echo "Implemente o comando de cópia adequado ao seu armazenamento externo."
# rsync -av --delete "${SOURCE}/" "${TARGET}/"
