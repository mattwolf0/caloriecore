$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$androidDebugBridge = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"

if ($env:JAVA_HOME) {
    $javaPath = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (-not (Test-Path -LiteralPath $javaPath)) {
        throw "JAVA_HOME is set but java.exe was not found at $javaPath"
    }
} elseif (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Java was not found. Install a JDK or set JAVA_HOME before running this script."
}

if (-not (Test-Path -LiteralPath $androidDebugBridge)) {
    throw "ADB not found at $androidDebugBridge"
}

Set-Location $repoRoot

$readyDeviceLines = & $androidDebugBridge devices | Select-Object -Skip 1 | Where-Object { $_ -match "\S+\s+device$" }
$readyDeviceIds = @($readyDeviceLines | ForEach-Object { ($_ -split "\s+")[0] })
$phoneSerial = $env:ANDROID_SERIAL

if (-not $phoneSerial) {
    $phoneSerial = @($readyDeviceIds | Where-Object { $_ -like "emulator-*" } | Select-Object -First 1)[0]
    if (-not $phoneSerial -and $readyDeviceIds.Count -eq 1) {
        $phoneSerial = $readyDeviceIds[0]
    }
}

if (-not $phoneSerial) {
    throw "No Android device selected. Connect one device, or set ANDROID_SERIAL to one of: $($readyDeviceIds -join ', ')"
}

$env:ANDROID_SERIAL = $phoneSerial
Write-Host "Using Android device: $phoneSerial"

& .\gradlew.bat :app:installDebug --console=plain
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$devPackage = "app.caloriecore.debug"
$launcherActivity = "app.caloriecore.MainActivity"
& $androidDebugBridge -s $phoneSerial shell am start -n "$devPackage/$launcherActivity"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
