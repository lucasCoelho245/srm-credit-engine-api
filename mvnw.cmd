@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_BASE=%USERPROFILE%\.m2\wrapper\dists
set MAVEN_HOME=%MAVEN_BASE%\apache-maven-%MAVEN_VERSION%
set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd

where mvn >nul 2>&1
if not errorlevel 1 goto run_global_maven

if exist "%MVN_CMD%" goto run_downloaded_maven

call :download_maven
if errorlevel 1 exit /b %ERRORLEVEL%

if exist "%MVN_CMD%" goto run_downloaded_maven

echo ERROR: Maven not found and automatic download failed.
exit /b 1

:run_global_maven
mvn %*
exit /b %ERRORLEVEL%

:run_downloaded_maven
"%MVN_CMD%" %*
exit /b %ERRORLEVEL%

:download_maven
echo Downloading Maven %MAVEN_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $base=$env:MAVEN_BASE; $version=$env:MAVEN_VERSION; $zip=Join-Path $base ('apache-maven-' + $version + '-bin.zip'); New-Item -ItemType Directory -Force -Path $base | Out-Null; Invoke-WebRequest -UseBasicParsing -Uri ('https://archive.apache.org/dist/maven/maven-3/' + $version + '/binaries/apache-maven-' + $version + '-bin.zip') -OutFile $zip; Expand-Archive -Force -Path $zip -DestinationPath $base"
exit /b %ERRORLEVEL%
