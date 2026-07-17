$ErrorActionPreference = "Stop"

$docsDir = "C:\Java\JetBrains 2025.3\GUO\docs"
$imgDir = Join-Path $docsDir "images"
$outPath = Join-Path $docsDir "gateway-redis-rate-limit.docx"

function Add-Text($doc, $text, $size, $bold, $alignCenter) {
    $end = $doc.Content.End - 1
    $range = $doc.Range($end, $end)
    $range.Text = ($text + [char]13)
    $range.Font.NameFarEast = "Microsoft YaHei"
    $range.Font.Name = "Calibri"
    $range.Font.Size = $size
    $range.Font.Bold = $bold
    if ($alignCenter) {
        $range.ParagraphFormat.Alignment = 1
    } else {
        $range.ParagraphFormat.Alignment = 0
    }
    $range.InsertParagraphAfter() | Out-Null
}

function Add-Image($doc, $path, $widthCm) {
    if (-not (Test-Path $path)) { throw "Missing image: $path" }
    $end = $doc.Content.End - 1
    $range = $doc.Range($end, $end)
    $shape = $range.InlineShapes.AddPicture($path, $false, $true)
    $shape.Width = [single]($widthCm * 28.35)
    $range.InsertParagraphAfter() | Out-Null
    Add-Text $doc "(See figure above while reading source code.)" 9 $false $false
}

$word = $null
$doc = $null
try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = 0
    $doc = $word.Documents.Add()

    Add-Text $doc "Gateway + Redis IP Rate Limit Study Notes" 20 $true $true
    Add-Text $doc "Beginner Review Edition (with diagrams)" 13 $true $true
    Add-Text $doc "Module: guo-gateway" 11 $false $false
    Add-Text $doc "Core class: IpRateLimitFilter" 11 $false $false
    Add-Text $doc "Rule: same IP, GET /users/{id}, max 3 requests per time window; over limit returns 429." 11 $false $false
    Add-Text $doc "Review order: diagrams first -> syntax -> source code -> self-test." 11 $false $false

    Add-Text $doc "1. Problem this chapter solves" 16 $true $false
    Add-Text $doc "1.1 Business rule" 13 $true $false
    Add-Text $doc "- Who: same client IP" 11 $false $false
    Add-Text $doc "- API: GET /users/{id} (getById)" 11 $false $false
    Add-Text $doc "- Window: WIDOW=10 seconds in code (can change; original homework often 1s)" 11 $false $false
    Add-Text $doc "- Limit: max 3 times" 11 $false $false
    Add-Text $doc "- Over limit: HTTP 429 + JSON, do not forward to user-service" 11 $false $false
    Add-Text $doc "1.2 Why put it on Gateway" 13 $true $false
    Add-Text $doc "- Unified entry, protect backend once" 11 $false $false
    Add-Text $doc "- Blocked requests never reach business service" 11 $false $false
    Add-Text $doc "One line: rate limit is the door guard, not the clerk." 11 $false $false

    Add-Text $doc "2. Concepts: Redis and Gateway" 16 $true $false
    Add-Text $doc "2.1 Redis" 13 $true $false
    Add-Text $doc "Redis is an in-memory Key-Value store. Here we use String + INCR + EXPIRE for fixed-window counting." 11 $false $false
    Add-Text $doc "- Fast counting" 11 $false $false
    Add-Text $doc "- TTL means window end" 11 $false $false
    Add-Text $doc "- Shared across gateway instances" 11 $false $false
    Add-Text $doc "2.2 Gateway" 13 $true $false
    Add-Text $doc "API Gateway is the unified entry. guo-gateway uses port 8088. /users/** -> user-service; /departments/** -> dept-service." 11 $false $false
    Add-Text $doc "Gateway is WebFlux, so use ReactiveStringRedisTemplate (not blocking StringRedisTemplate)." 11 $false $false

    Add-Text $doc "3. Cache vs Rate Limit" 16 $true $false
    Add-Text $doc "- Department cache: store department JSON to speed reads" 11 $false $false
    Add-Text $doc "- IP rate limit: store request count to control traffic" 11 $false $false
    Add-Text $doc "Analogy: cache = bulletin board; rate limit = security guard." 11 $false $false
    $cacheImg = Join-Path $imgDir "diagram-06-cache-vs-limit.png"
    if (Test-Path $cacheImg) { Add-Image $doc $cacheImg 15 }

    Add-Text $doc "4. Architecture flow (diagram)" 16 $true $false
    Add-Text $doc "Must test via: http://localhost:8088/users/1 . Direct 8081 bypasses the filter." 11 $false $false
    Add-Image $doc (Join-Path $imgDir "diagram-01-architecture.png") 15

    Add-Text $doc "5. Basic Java syntax checklist" 16 $true $false
    Add-Text $doc "5.1 Type / variable / method" 13 $true $false
    Add-Text $doc "Example: ServerHttpRequest request = exchange.getRequest();" 11 $false $false
    Add-Text $doc "- ServerHttpRequest = type (what box)" 11 $false $false
    Add-Text $doc "- request = variable name" 11 $false $false
    Add-Text $doc "- getRequest() = method call" 11 $false $false
    Add-Text $doc "5.2 Constructor vs normal method" 13 $true $false
    Add-Text $doc "- Constructor: same name as class; runs when object is created; initializes dependencies" 11 $false $false
    Add-Text $doc "- Normal method: custom name; can run many times after object exists" 11 $false $false
    Add-Image $doc (Join-Path $imgDir "diagram-05-ctor-vs-method.png") 15
    Add-Text $doc "5.3 exchange vs chain" 13 $true $false
    Add-Text $doc "- exchange: request/response package; read path/method; write 429" 11 $false $false
    Add-Text $doc "- chain: pass-through; chain.filter(exchange) continues the pipeline" 11 $false $false
    Add-Image $doc (Join-Path $imgDir "diagram-04-exchange-chain.png") 15
    Add-Text $doc "5.4 Other syntax" 13 $true $false
    Add-Text $doc "- != == && || !" 11 $false $false
    Add-Text $doc "- condition ? A : B  (ternary)" 11 $false $false
    Add-Text $doc "- param -> { ... }  (lambda)" 11 $false $false
    Add-Text $doc "- Mono = async result later; flatMap = continue after result arrives" 11 $false $false

    Add-Text $doc "6. Class structure" 16 $true $false
    Add-Text $doc "- Fields: log, USER_GET_BY_ID, LIMIT, WIDOW, redisTemplate" 11 $false $false
    Add-Text $doc "- Constructor: inject redisTemplate" 11 $false $false
    Add-Text $doc "- Main: filter" 11 $false $false
    Add-Text $doc "- Helpers: resolveClientIp, writeTooManyRequests" 11 $false $false
    Add-Text $doc "- Order: getOrder returns -100" 11 $false $false

    Add-Text $doc "7. Filter decision flow (diagram)" 16 $true $false
    Add-Image $doc (Join-Path $imgDir "diagram-02-filter-flow.png") 12
    Add-Text $doc "7.1 Sync mental model" 13 $true $false
    Add-Text $doc "1) read method and path" 11 $false $false
    Add-Text $doc "2) if not GET /users/{number} -> pass" 11 $false $false
    Add-Text $doc "3) resolve IP and build redisKey" 11 $false $false
    Add-Text $doc "4) count = Redis INCR" 11 $false $false
    Add-Text $doc "5) if count==1 -> EXPIRE start window" 11 $false $false
    Add-Text $doc "6) if count>3 -> 429 else pass" 11 $false $false
    Add-Text $doc "7.2 Why EXPIRE only when count==1" 13 $true $false
    Add-Text $doc "Only first hit starts countdown. Refreshing EXPIRE every time would stretch the window forever." 11 $false $false

    Add-Text $doc "8. Redis window counting (diagram)" 16 $true $false
    Add-Image $doc (Join-Path $imgDir "diagram-03-redis-window.png") 15
    Add-Text $doc "Key example: rate:limit:127.0.0.1:GET:/users/1" 11 $false $false
    Add-Text $doc "- 1st to 3rd: allow (often 200)" 11 $false $false
    Add-Text $doc "- 4th+: block (429)" 11 $false $false
    Add-Text $doc "- After TTL: key deleted, count restarts from 1" 11 $false $false

    Add-Text $doc "9. Helper methods" 16 $true $false
    Add-Text $doc "9.1 resolveClientIp" 13 $true $false
    Add-Text $doc "- Prefer X-Forwarded-For first segment" 11 $false $false
    Add-Text $doc "- Else remote address" 11 $false $false
    Add-Text $doc "- Else unknown" 11 $false $false
    Add-Text $doc "9.2 writeTooManyRequests" 13 $true $false
    Add-Text $doc "- Must use Response, not Request" 11 $false $false
    Add-Text $doc "- Set 429 + JSON body" 11 $false $false
    Add-Text $doc "- Do not call chain.filter" 11 $false $false
    Add-Text $doc "9.3 getOrder" 13 $true $false
    Add-Text $doc "Return -100 so this filter runs early. Called by framework." 11 $false $false

    Add-Text $doc "10. Test and troubleshooting" 16 $true $false
    Add-Text $doc "Correct URL: GET http://localhost:8088/users/1" 11 $false $false
    Add-Text $doc "If Postman always 200:" 13 $true $false
    Add-Text $doc "- Hitting 8081 instead of 8088" 11 $false $false
    Add-Text $doc "- Hitting list API /users?page=1" 11 $false $false
    Add-Text $doc "- Clicks slower than window" 11 $false $false
    Add-Text $doc "- Gateway not restarted after code change" 11 $false $false
    Add-Text $doc "PowerShell burst test:" 13 $true $false
    Add-Text $doc "1..5 | ForEach-Object { curl.exe -s -o NUL -w status=%{http_code} http://localhost:8088/users/1 }" 10 $false $false
    Add-Text $doc "Expect: first statuses 200, later 429." 11 $false $false

    Add-Text $doc "11. Interview points" 16 $true $false
    Add-Text $doc "- Why String+INCR+EXPIRE: atomic count + TTL fixed window" 11 $false $false
    Add-Text $doc "- Why EXPIRE only at count==1: avoid stretching window" 11 $false $false
    Add-Text $doc "- Why gateway: unified entry and backend protection" 11 $false $false
    Add-Text $doc "- Cache vs limit: data vs counter" 11 $false $false
    Add-Text $doc "- Why reactive Redis: WebFlux must not block" 11 $false $false

    Add-Text $doc "12. Self-test" 16 $true $false
    Add-Text $doc "1. What are exchange and chain for?" 11 $false $false
    Add-Text $doc "2. Why read path from exchange, not chain?" 11 $false $false
    Add-Text $doc "3. Constructor vs filter: when each runs?" 11 $false $false
    Add-Text $doc "4. Why redisKey includes IP and path?" 11 $false $false
    Add-Text $doc "5. curl gets 429 but Postman always OK: common reason?" 11 $false $false
    Add-Text $doc "6. Does log.warn show in Postman?" 11 $false $false
    Add-Text $doc "Answers:" 12 $true $false
    Add-Text $doc "1) exchange=data package; chain=pass-through" 11 $false $false
    Add-Text $doc "2) path is in request inside exchange" 11 $false $false
    Add-Text $doc "3) constructor once at create; filter per request" 11 $false $false
    Add-Text $doc "4) count by user and by API separately" 11 $false $false
    Add-Text $doc "5) wrong port/path or clicks too slow" 11 $false $false
    Add-Text $doc "6) no; only gateway console. Postman shows 429 body" 11 $false $false

    Add-Text $doc "13. Review checklist" 16 $true $false
    Add-Text $doc "[ ] Draw: client -> gateway -> filter -> Redis -> allow/429" 11 $false $false
    Add-Text $doc "[ ] Distinguish type / variable / method" 11 $false $false
    Add-Text $doc "[ ] Explain constructor vs normal method" 11 $false $false
    Add-Text $doc "[ ] Explain why EXPIRE only when count==1" 11 $false $false
    Add-Text $doc "[ ] Know must use 8088 and /users/{number}" 11 $false $false
    Add-Text $doc "[ ] Know cache and rate-limit both use Redis differently" 11 $false $false

    Add-Text $doc "Appendix" 16 $true $false
    Add-Text $doc "- IpRateLimitFilter.java" 11 $false $false
    Add-Text $doc "- guo-gateway pom.xml / application.yml" 11 $false $false
    Add-Text $doc "- Chinese markdown: docs/网关-Redis-IP限流分析文档.md" 11 $false $false
    Add-Text $doc "- Images: docs/images/" 11 $false $false
    Add-Text $doc "Note: Full Chinese detailed text is in the markdown file; this Word file focuses on diagrams + structured review." 11 $false $false

    if (Test-Path $outPath) { Remove-Item -LiteralPath $outPath -Force }
    $doc.SaveAs2($outPath, 12)
    Write-Output "SAVED:$outPath"
}
finally {
    if ($null -ne $doc) {
        try { $doc.Close($false) } catch {}
        [void][System.Runtime.InteropServices.Marshal]::FinalReleaseComObject($doc)
    }
    if ($null -ne $word) {
        try { $word.Quit() } catch {}
        [void][System.Runtime.InteropServices.Marshal]::FinalReleaseComObject($word)
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}
