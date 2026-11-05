$ErrorActionPreference = 'Stop'

& (Join-Path $PSScriptRoot 'mvnw.cmd') -B -f (Join-Path $PSScriptRoot 'pom.xml') verify
if ($LASTEXITCODE -ne 0) { throw 'Maven verification failed.' }
