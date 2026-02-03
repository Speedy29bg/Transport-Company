$ErrorActionPreference = "Stop"

$localMaven = Join-Path $env:USERPROFILE "maven\apache-maven-3.9.6\bin\mvn.cmd"

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvn = "mvn"
} elseif (Test-Path $localMaven) {
    $mvn = $localMaven
} else {
    Write-Error "Maven not found. Install Maven or add it to PATH."
    exit 1
}

& $mvn test
