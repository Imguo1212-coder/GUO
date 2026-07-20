chcp 65001 > $null
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$ErrorActionPreference = "Stop"

$conf = Join-Path $PSScriptRoot "nginx.conf"
if (-not (Test-Path $conf)) {
    throw "找不到 nginx.conf: $conf"
}

cmd /c "docker rm -f nginx >nul 2>&1"

docker run -d `
  --name nginx `
  -p 80:80 `
  -v "${conf}:/etc/nginx/nginx.conf:ro" `
  nginx:1.27-alpine

Write-Host "Nginx 已启动: http://localhost/"
Write-Host "会转发到宿主机 Gateway: http://host.docker.internal:8088"
Write-Host "测试: http://localhost/users/1"
Write-Host "查看日志: docker logs -f nginx"
Write-Host "停止: docker stop nginx"