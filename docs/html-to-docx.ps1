$ErrorActionPreference = "Stop"
$html = "C:\Java\JetBrains 2025.3\GUO\docs\网关-Redis-IP限流分析文档.html"
$docx = "C:\Java\JetBrains 2025.3\GUO\docs\网关-Redis-IP限流分析文档.docx"

$assets = "C:\Users\Administrator\.cursor\projects\c-Java-JetBrains-2025-3-GUO\assets"
$imgDir = "C:\Java\JetBrains 2025.3\GUO\docs\images"
New-Item -ItemType Directory -Force -Path $imgDir | Out-Null
@(
  "diagram-01-architecture.png",
  "diagram-02-filter-flow.png",
  "diagram-03-redis-window.png",
  "diagram-04-exchange-chain.png",
  "diagram-05-ctor-vs-method.png",
  "diagram-06-cache-vs-limit.png"
) | ForEach-Object {
  $src = Join-Path $assets $_
  if (Test-Path $src) { Copy-Item $src $imgDir -Force }
}

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $null
try {
  $doc = $word.Documents.Open($html)
  if (Test-Path -LiteralPath $docx) { Remove-Item -LiteralPath $docx -Force }
  $doc.SaveAs2($docx, 16)
  Write-Output "SAVED:$docx"
}
finally {
  if ($doc) { try { $doc.Close($false) } catch {} }
  try { $word.Quit() } catch {}
  [GC]::Collect()
}
