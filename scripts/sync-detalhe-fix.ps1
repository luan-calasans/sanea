$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$hostUmbrel = "umbrel@192.168.0.67"
$remoteBase = "/home/umbrel/santos-chamados"

$files = @(
  "src/main/java/br/com/luancalasans/santoschamados/chamado/dto/ChamadoDetalheDTO.java",
  "src/main/java/br/com/luancalasans/santoschamados/chamado/dto/AnexoItemDTO.java",
  "src/main/java/br/com/luancalasans/santoschamados/chamado/dto/StatusHistoricoItemDTO.java",
  "src/main/java/br/com/luancalasans/santoschamados/chamado/service/ChamadoService.java",
  "src/main/java/br/com/luancalasans/santoschamados/chamado/controller/ChamadoController.java",
  "src/main/java/br/com/luancalasans/santoschamados/mensagem/repository/MensagemChamadoRepository.java",
  "src/main/java/br/com/luancalasans/santoschamados/mensagem/service/MensagemService.java",
  "src/main/java/br/com/luancalasans/santoschamados/mensagem/controller/MensagemController.java",
  "src/main/java/br/com/luancalasans/santoschamados/mensagem/dto/MensagemItemDTO.java",
  "src/main/java/br/com/luancalasans/santoschamados/anexo/repository/AnexoRepository.java",
  "src/main/java/br/com/luancalasans/santoschamados/anexo/service/AnexoService.java",
  "src/main/resources/templates/chamado/detalhe.html",
  "src/main/resources/templates/fragments/mensagens.html"
)

foreach ($rel in $files) {
  $local = Join-Path $root ($rel -replace "/", "\")
  $remoteDir = Split-Path "$remoteBase/$rel" -Parent
  $remoteDirUnix = ($remoteDir -replace "\\", "/")
  Write-Host ">> $rel"
  ssh $hostUmbrel "mkdir -p '$remoteDirUnix'"
  scp $local "${hostUmbrel}:${remoteBase}/${rel}"
}

Write-Host ""
Write-Host "Arquivos enviados. No Umbrel rode:"
Write-Host "  cd ~/santos-chamados"
Write-Host "  docker compose build santos-chamados-app && docker compose up -d santos-chamados-app"
