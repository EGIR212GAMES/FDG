# Bootstrap a Fabric server (MC 26.3, loader 0.19.5) for Bedrock 26.52 integration tests.
$ErrorActionPreference = "Stop"
$mc = "26.3"
$loader = "0.19.5"
$installer = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.0/fabric-installer-1.0.0.jar"

function Get-FabricInstaller {
    if (-not (Test-Path "fabric-installer.jar")) {
        Invoke-WebRequest -Uri $installer -OutFile "fabric-installer.jar"
    }
}

Get-FabricInstaller
java -jar fabric-installer.jar server -mcversion $mc -loader $loader -dir .

Write-Host "Fabric server ready. Put mods into mods\ and configs as described in README.md."
