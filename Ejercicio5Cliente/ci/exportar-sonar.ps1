param([string]$ReportDirectory = (Join-Path $PSScriptRoot '../entrega/parte2/seguridad'))
$ErrorActionPreference='Stop'
$analysisSecret=$env:SONAR_TOKEN
if(-not $analysisSecret){$analysisSecret=(Get-Content (Join-Path $PSScriptRoot '../.secrets/sonar-token') -Raw).Trim()}
$analysisHeaders=@{Authorization='Basic '+[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($analysisSecret+':'))}
New-Item -ItemType Directory -Force $ReportDirectory | Out-Null
$summary=@{}
foreach($projectKey in @('gestor-frontend','gestor-backend','gestor-database')){
    $prefix=$projectKey.Replace('gestor-','')
    $gate=Invoke-RestMethod "http://localhost:9000/api/qualitygates/project_status?projectKey=$projectKey" -Headers $analysisHeaders
    $measures=$null
    try { $measures=Invoke-RestMethod "http://localhost:9000/api/measures/component?component=$projectKey&metricKeys=ncloc,coverage,bugs,vulnerabilities,code_smells,security_hotspots,duplicated_lines_density" -Headers $analysisHeaders } catch { $measures=@{nota="El token de analisis no permite leer estas metricas; consultar capturas y Quality Gate."} }
    $allIssues=@();$page=1
    do{
        $batch=Invoke-RestMethod "http://localhost:9000/api/issues/search?componentKeys=$projectKey&resolved=false&ps=500&p=$page" -Headers $analysisHeaders
        $allIssues+=@($batch.issues);$page++
    }while($allIssues.Count -lt $batch.total)
    $gate|ConvertTo-Json -Depth 30|Set-Content -Encoding utf8 (Join-Path $ReportDirectory "sonar-$prefix-quality-gate.json")
    $measures|ConvertTo-Json -Depth 30|Set-Content -Encoding utf8 (Join-Path $ReportDirectory "sonar-$prefix-metricas.json")
    @{total=$allIssues.Count;issues=$allIssues}|ConvertTo-Json -Depth 50|Set-Content -Encoding utf8 (Join-Path $ReportDirectory "sonar-$prefix-incidencias.json")
    $summary[$projectKey]=@{qualityGate=$gate.projectStatus.status;issues=$allIssues.Count;measures=$measures.component.measures}
}
$summary|ConvertTo-Json -Depth 30|Set-Content -Encoding utf8 (Join-Path $ReportDirectory 'resumen-sonar.json')
$summary.Keys|ForEach-Object {[pscustomobject]@{Proyecto=$_;QualityGate=$summary[$_].qualityGate;Incidencias=$summary[$_].issues}}
