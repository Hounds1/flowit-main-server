param(
    [Parameter(Mandatory = $true)]
    [string] $Task
)

$ErrorActionPreference = 'Stop'
$env:FLOWIT_GRADLE_FALLBACK_TASK = $Task

try {
    & (Join-Path $PSScriptRoot 'local-docker.ps1') $Task
    exit $LASTEXITCODE
}
catch {
    Write-Error $_
    exit 1
}
