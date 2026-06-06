[CmdletBinding()]
param(
    [string]$AdbPath = "",
    [string]$Serial = "",
    [string]$Package = "com.wuxianggujun.tinaide",
    [int]$LogcatLines = 5000,
    [switch]$ClearLogcat,
    [switch]$Launch,
    [switch]$TapBuild,
    [int]$BuildTapX = 560,
    [int]$BuildTapY = 72,
    [int]$WaitSeconds = 8
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Write-Section([string]$Title) {
    Write-Host ""
    Write-Host "=== $Title ===" -ForegroundColor Yellow
}

function Write-Info([string]$Message) {
    Write-Host "[i] $Message" -ForegroundColor Cyan
}

function Write-WarnLine([string]$Message) {
    Write-Host "[!] $Message" -ForegroundColor Yellow
}

function Write-ErrLine([string]$Message) {
    Write-Host "[x] $Message" -ForegroundColor Red
}

function Resolve-Adb {
    param([string]$Requested)

    if (-not [string]::IsNullOrWhiteSpace($Requested)) {
        if (Test-Path $Requested -PathType Leaf) {
            return (Resolve-Path $Requested).Path
        }
        $fromPath = Get-Command $Requested -ErrorAction SilentlyContinue
        if ($fromPath) {
            return $fromPath.Source
        }
        throw "ADB not found: $Requested"
    }

    $commonPaths = @(
        "D:\Program Files\Microvirt\MEmu\adb.exe",
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "$env:ANDROID_HOME\platform-tools\adb.exe",
        "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe"
    )

    foreach ($path in $commonPaths) {
        if (-not [string]::IsNullOrWhiteSpace($path) -and (Test-Path $path -PathType Leaf)) {
            return (Resolve-Path $path).Path
        }
    }

    $adb = Get-Command "adb" -ErrorAction SilentlyContinue
    if ($adb) {
        return $adb.Source
    }

    throw "ADB not found. Pass -AdbPath or add adb to PATH."
}

function Invoke-Adb {
    param(
        [string]$Adb,
        [string]$Device,
        [string[]]$AdbArgs
    )

    $allArgs = @()
    if (-not [string]::IsNullOrWhiteSpace($Device)) {
        $allArgs += @("-s", $Device)
    }
    $allArgs += $AdbArgs

    & $Adb @allArgs
}

function Resolve-DeviceSerial {
    param(
        [string]$Adb,
        [string]$RequestedSerial
    )

    if (-not [string]::IsNullOrWhiteSpace($RequestedSerial)) {
        return $RequestedSerial
    }

    $devices = & $Adb devices |
        Select-Object -Skip 1 |
        Where-Object { $_ -match "\sdevice$" } |
        ForEach-Object { ($_ -split "\s+")[0] }

    if (-not $devices -or $devices.Count -eq 0) {
        throw "No adb device is connected."
    }
    if ($devices.Count -gt 1) {
        throw "Multiple adb devices are connected. Pass -Serial. Devices: $($devices -join ', ')"
    }
    return $devices[0]
}

function Get-Prop {
    param(
        [string]$Adb,
        [string]$Device,
        [string]$Name
    )

    $value = Invoke-Adb -Adb $Adb -Device $Device -AdbArgs @("shell", "getprop", $Name)
    return (($value | Out-String).Trim())
}

function Get-PackageLine {
    param(
        [string]$Adb,
        [string]$Device,
        [string]$PackageName,
        [string]$Pattern
    )

    $dump = Invoke-Adb -Adb $Adb -Device $Device -AdbArgs @("shell", "dumpsys", "package", $PackageName)
    $line = $dump | Select-String -Pattern $Pattern | Select-Object -First 1
    if ($line) {
        return $line.Line.Trim()
    }
    return "<missing>"
}

$adb = Resolve-Adb -Requested $AdbPath
$device = Resolve-DeviceSerial -Adb $adb -RequestedSerial $Serial

Write-Section "Device"
Write-Info "ADB: $adb"
Write-Info "Serial: $device"
Write-Host ("Android: {0} / SDK {1}" -f (Get-Prop -Adb $adb -Device $device -Name "ro.build.version.release"), (Get-Prop -Adb $adb -Device $device -Name "ro.build.version.sdk"))
Write-Host ("ABI: {0}" -f (Get-Prop -Adb $adb -Device $device -Name "ro.product.cpu.abi"))
Write-Host ("Package: {0}" -f $Package)
Write-Host (Get-PackageLine -Adb $adb -Device $device -PackageName $Package -Pattern "versionName")
Write-Host (Get-PackageLine -Adb $adb -Device $device -PackageName $Package -Pattern "versionCode")
Write-Host (Get-PackageLine -Adb $adb -Device $device -PackageName $Package -Pattern "lastUpdateTime")

if ($Launch) {
    Write-Section "Launch"
    Write-Info "Launching $Package"
    Invoke-Adb -Adb $adb -Device $device -AdbArgs @(
        "shell",
        "monkey",
        "-p",
        $Package,
        "-c",
        "android.intent.category.LAUNCHER",
        "1"
    ) | Out-Host
}

if ($ClearLogcat) {
    Write-Section "Logcat"
    Write-Info "Clearing logcat"
    Invoke-Adb -Adb $adb -Device $device -AdbArgs @("logcat", "-c") | Out-Null
}

if ($TapBuild) {
    Write-Section "Build Trigger"
    Write-Info "Tapping build button at x=$BuildTapX y=$BuildTapY"
    Invoke-Adb -Adb $adb -Device $device -AdbArgs @("shell", "input", "tap", "$BuildTapX", "$BuildTapY") | Out-Null
    Write-Info "Waiting $WaitSeconds seconds for build logs"
    Start-Sleep -Seconds $WaitSeconds
}

Write-Section "Native Build Logs"
$rawLog = Invoke-Adb -Adb $adb -Device $device -AdbArgs @("logcat", "-d", "-t", "$LogcatLines")
$patterns = @(
    "NativeCMakeBuildExecutor",
    "Resolved build tool",
    "CMAKE_MAKE_PROGRAM",
    "toolchain-direct-shims",
    "retrying with real",
    "detected version of Ninja",
    "linker64",
    "CMake configure failure",
    "Build finished",
    "Compile built-only success",
    "Exported build artifact",
    "ninja:"
)

$filtered = $rawLog | Select-String -Pattern ($patterns -join "|")
if ($filtered) {
    $filtered | ForEach-Object { $_.Line }
} else {
    Write-WarnLine "No matching native build logs found in last $LogcatLines logcat lines."
}

$joined = ($filtered | ForEach-Object { $_.Line }) -join "`n"
$hasLinker64NinjaError = $joined -match "detected version of Ninja" -and $joined -match "linker64"
$hasSuccess = $joined -match "Build finished" -or $joined -match "Compile built-only success" -or $joined -match "Exported build artifact"
$hasDirectShim = $joined -match "toolchain-direct-shims"

Write-Section "Verdict"
if ($hasLinker64NinjaError) {
    Write-ErrLine "FAILED: CMake still detected linker64 text as Ninja version."
    exit 2
}

if ($hasSuccess) {
    if ($hasDirectShim) {
        Write-Info "PASSED: native build succeeded and CMAKE_MAKE_PROGRAM used direct shim."
    } else {
        Write-Info "PASSED: native build succeeded."
    }
    exit 0
}

Write-WarnLine "INCONCLUSIVE: no linker64 Ninja error was found, but no successful build marker was found either."
Write-WarnLine "Open a CMake project, trigger Build, then rerun with -TapBuild or increase -LogcatLines."
exit 3
