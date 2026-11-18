@echo off
rem JAVA_HOME = JDK 25 (26.x builds), LEGACY_JAVA_HOME = JDK 8 or 17 (1.8.9 build)
setlocal
cd /d "%~dp0"
if "%LEGACY_JAVA_HOME%"=="" (
  echo Set LEGACY_JAVA_HOME to a JDK 8 or 17 for the 1.8.9 build
  exit /b 1
)

if exist artifacts rmdir /s /q artifacts
mkdir artifacts

call gradlew.bat buildAll || exit /b 1
copy /y build\libs\*.jar artifacts\ >nul

pushd legacy-1.8.9
set "MODERN_JAVA_HOME=%JAVA_HOME%"
set "JAVA_HOME=%LEGACY_JAVA_HOME%"
call gradlew.bat build || (popd & exit /b 1)
set "JAVA_HOME=%MODERN_JAVA_HOME%"
for %%f in (build\libs\gencount-1.8.9-*.jar) do (
  echo %%f | findstr /i /c:"-dev" /c:"-sources" >nul || copy /y "%%f" ..\artifacts\ >nul
)
popd

echo Done. Jars are in artifacts\
dir /b artifacts
