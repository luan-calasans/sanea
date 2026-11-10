#!/bin/sh
set -eu

PROJECT_DIR="${PROJECT_DIR:-/home/umbrel/santos-chamados}"

echo "==> Verificando porta 18473"
if command -v ss >/dev/null 2>&1; then
  if sudo ss -ltnp | grep -q ':18473 '; then
    echo "Porta 18473 já está em uso. Interrompendo deploy."
    sudo ss -ltnp | grep ':18473 ' || true
    exit 1
  fi
fi

echo "==> Containers e portas atuais"
sudo docker ps --format "table {{.Names}}\t{{.Ports}}"

cd "${PROJECT_DIR}"

if [ ! -f .env ]; then
  echo "Crie ${PROJECT_DIR}/.env a partir de .env.example antes do deploy."
  exit 1
fi

echo "==> Build e start"
sudo docker compose build
sudo docker compose up -d
sudo docker compose ps
sudo docker compose logs --tail=100 santos-chamados-app

echo "==> Teste local sugerido: http://192.168.0.67:18473/actuator/health"
echo "==> Após healthy, publique no Cloudflare Tunnel existente:"
echo "    Hostname: app.luancalasans.com.br"
echo "    Service:  http://192.168.0.67:18473"
