$ErrorActionPreference = "Stop"

$jarPath = "d:\Coding\GitHub\Transport-Company\Transport Company\target\transport-company-1.0.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Error "Jar not found at: $jarPath"
    exit 1
}

function Invoke-TransportCommand {
    param([string[]]$CommandArgs)
    $display = $CommandArgs -join " "
    Write-Host "`n> $display" -ForegroundColor Cyan
    $output = & java -jar $jarPath @CommandArgs 2>&1
    $output | ForEach-Object { Write-Host $_ }
    return $output
}

function Get-CreatedId {
    param([string[]]$Output)
    $match = $Output | Select-String -Pattern "ID:\s*(\d+)" | Select-Object -First 1
    if ($match) { return [int]$match.Matches[0].Groups[1].Value }
    throw "Could not parse created ID from output."
}

Write-Host "Running DB smoke tests..." -ForegroundColor Green

# 1) Company: add -> get -> edit -> list
$companyOut = Invoke-TransportCommand -CommandArgs @("company", "add", "Smoke Test Co", "Sofia, Bulgaria")
$companyId = Get-CreatedId $companyOut
Invoke-TransportCommand -CommandArgs @("company", "get", "$companyId")
Invoke-TransportCommand -CommandArgs @("company", "edit", "$companyId", "-n", "Smoke Test Co Updated", "-a", "Plovdiv, Bulgaria")
Invoke-TransportCommand -CommandArgs @("company", "list")

# 2) Client: add -> get -> edit -> list
$clientOut = Invoke-TransportCommand -CommandArgs @("client", "add", "Smoke Client", "+359888000000", "smoke.client@example.com", "-t", "PERSON")
$clientId = Get-CreatedId $clientOut
Invoke-TransportCommand -CommandArgs @("client", "get", "$clientId")
Invoke-TransportCommand -CommandArgs @("client", "edit", "$clientId", "-n", "Smoke Client Updated")
Invoke-TransportCommand -CommandArgs @("client", "list")

# 3) Employee: add -> get -> edit -> list
$employeeOut = Invoke-TransportCommand -CommandArgs @("employee", "add", "$companyId", "Smoke Driver", "2500", "-r", "DRIVER", "-q", "PASSENGERS_12_PLUS")
$employeeId = Get-CreatedId $employeeOut
Invoke-TransportCommand -CommandArgs @("employee", "get", "$employeeId")
Invoke-TransportCommand -CommandArgs @("employee", "edit", "$employeeId", "-s", "2700")
Invoke-TransportCommand -CommandArgs @("employee", "list", "--sort=salary")

# 4) Vehicle: add -> get -> edit -> list
$vehicleOut = Invoke-TransportCommand -CommandArgs @("vehicle", "add", "$companyId", "SMK-123", "BUS", "--seats", "45")
$vehicleId = Get-CreatedId $vehicleOut
Invoke-TransportCommand -CommandArgs @("vehicle", "get", "$vehicleId")
Invoke-TransportCommand -CommandArgs @("vehicle", "edit", "$vehicleId", "--seats", "50")
Invoke-TransportCommand -CommandArgs @("vehicle", "list", "$companyId")

# 5) Cleanup: delete in reverse order
Invoke-TransportCommand -CommandArgs @("vehicle", "delete", "$vehicleId")
Invoke-TransportCommand -CommandArgs @("employee", "delete", "$employeeId")
Invoke-TransportCommand -CommandArgs @("client", "delete", "$clientId")
Invoke-TransportCommand -CommandArgs @("company", "delete", "$companyId")

Write-Host "`nDB smoke tests complete." -ForegroundColor Green
