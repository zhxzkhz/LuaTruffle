# Runs the CPU-time benchmark on the native image and on the JVM, compares the
# fastest sample per case against bench/baseline.json and exits non-zero when a
# case is slower than the allowed tolerance.
#
# Windows PowerShell 5.1+ only (the JVM classpath uses ';' as separator).
#
# Usage:
#   powershell -File scripts/bench-baseline.ps1
#   powershell -File scripts/bench-baseline.ps1 -Runs 5
#   powershell -File scripts/bench-baseline.ps1 -Update
#   powershell -File scripts/bench-baseline.ps1 -SkipJvm -SkipNative
[CmdletBinding()]
param(
    [int]$Runs = 3,
    [double]$Tolerance = 0,
    [switch]$Update,
    [switch]$SkipNative,
    [switch]$SkipJvm
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$benchScript = Join-Path $root 'bench\bench.lua'
$baselinePath = Join-Path $root 'bench\baseline.json'
$defaultTolerance = 1.25
$defaultMinSeconds = 0.08

if ($Runs -lt 1) { throw "Runs must be at least 1." }
if ($SkipNative -and $SkipJvm) { throw "Nothing to run: both platforms are skipped." }

$baseline = $null
if (Test-Path $baselinePath) {
    $baseline = Get-Content $baselinePath -Raw | ConvertFrom-Json
}

$effectiveTolerance = if ($Tolerance -gt 0) { $Tolerance }
    elseif ($baseline -and $baseline.tolerance) { [double]$baseline.tolerance }
    else { $defaultTolerance }
$minSeconds = if ($baseline -and $baseline.minSeconds) { [double]$baseline.minSeconds } else { $defaultMinSeconds }

function Get-BenchSamples {
    param([object[]]$Output, [string]$Platform)

    $samples = @{}
    foreach ($line in $Output) {
        $text = [string]$line
        if ($text -match '^BENCH\s+(\S+)\s+([0-9.]+)\s*$') {
            $name = $Matches[1]
            $value = [double]$Matches[2]
            if (-not $samples.ContainsKey($name)) { $samples[$name] = New-Object System.Collections.Generic.List[double] }
            $samples[$name].Add($value)
        }
    }
    if ($samples.Count -eq 0) { throw "$Platform produced no BENCH lines." }

    $fastest = @{}
    foreach ($name in $samples.Keys) { $fastest[$name] = ($samples[$name] | Measure-Object -Minimum).Minimum }
    return $fastest
}

function Invoke-NativeBench {
    $exe = Join-Path $root 'standalone\target\luanative.exe'
    if (-not (Test-Path $exe)) { throw "Native image not found at $exe. Build it with: mvn package -Pnative -DskipTests" }
    $output = & $exe $benchScript $Runs 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Native benchmark failed with exit code $LASTEXITCODE." }
    return Get-BenchSamples -Output $output -Platform 'native'
}

function Invoke-JvmBench {
    $launcherJar = Join-Path $root 'launcher\target\launcher-1.0.0-SNAPSHOT.jar'
    if (-not (Test-Path $launcherJar)) { throw "Launcher jar not found at $launcherJar. Build it with: mvn package -DskipTests" }
    $cpFile = Join-Path $root 'launcher\target\bench-classpath.txt'
    & mvn -q -pl launcher dependency:build-classpath "-Dmdep.outputFile=$cpFile"
    if ($LASTEXITCODE -ne 0) { throw "Failed to resolve the launcher classpath." }
    $classpath = (Get-Content $cpFile -Raw).Trim()
    $output = & java -ea '--enable-native-access=ALL-UNNAMED' '--sun-misc-unsafe-memory-access=allow' `
        -cp "$launcherJar;$classpath" com.zhhz.truffle.lua.launcher.LuaMain $benchScript $Runs 2>&1
    if ($LASTEXITCODE -ne 0) { throw "JVM benchmark failed with exit code $LASTEXITCODE." }
    return Get-BenchSamples -Output $output -Platform 'jvm'
}

$results = [ordered]@{}
if (-not $SkipNative) { Write-Host 'Running native benchmark...'; $results['native'] = Invoke-NativeBench }
if (-not $SkipJvm) { Write-Host 'Running JVM benchmark...'; $results['jvm'] = Invoke-JvmBench }

if ($Update) {
    $data = @{}
    if ($baseline) {
        foreach ($platform in 'native', 'jvm') {
            if ($baseline.$platform) {
                $data[$platform] = @{}
                foreach ($property in $baseline.$platform.PSObject.Properties) { $data[$platform][$property.Name] = $property.Value }
            }
        }
    }
    foreach ($platform in $results.Keys) { $data[$platform] = $results[$platform] }
    $data['tolerance'] = $effectiveTolerance
    $data['minSeconds'] = $minSeconds
    $data['updated'] = Get-Date -Format 'yyyy-MM-dd'
    $data | ConvertTo-Json -Depth 4 | Set-Content -Encoding UTF8 $baselinePath
    Write-Host "Updated $baselinePath"
}

$failed = $false
foreach ($platform in $results.Keys) {
    Write-Host ""
    Write-Host ("{0,-12} {1,-10} {2,10} {3,10} {4,10}  {5}" -f 'platform', 'case', 'measured', 'baseline', 'limit', 'status')
    $measured = $results[$platform]
    foreach ($name in ($measured.Keys | Sort-Object)) {
        $value = [double]$measured[$name]
        $reference = $null
        if ($baseline -and $baseline.$platform) {
            $property = $baseline.$platform.PSObject.Properties[$name]
            if ($property) { $reference = [double]$property.Value }
        }
        if ($null -eq $reference) {
            Write-Host ("{0,-12} {1,-10} {2,10:N3} {3,10} {4,10}  {5}" -f $platform, $name, $value, '-', '-', ($(if ($Update) { 'recorded' } else { 'missing' })))
            if (-not $Update) { $failed = $true }
            continue
        }
        $limit = [Math]::Max($minSeconds, $reference * $effectiveTolerance)
        $ok = $value -le $limit
        if (-not $ok) { $failed = $true }
        $status = if ($ok) { 'ok' } else { 'SLOWER' }
        Write-Host ("{0,-12} {1,-10} {2,10:N3} {3,10:N3} {4,10:N3}  {5}" -f $platform, $name, $value, $reference, $limit, $status)
    }
}

Write-Host ""
if ($failed) {
    Write-Host "Performance baseline check FAILED (tolerance $effectiveTolerance)."
    exit 1
}
Write-Host "Performance baseline check passed (tolerance $effectiveTolerance)."
exit 0
