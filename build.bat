@echo off
REM Compila o mod com JDK 8 (obrigatorio: ForgeGradle 2.1 / Gradle 2.14 nao rodam em Java 9+).
setlocal

set "JDK8=C:\Users\Kaio\.jdks\temurin-8"
if not exist "%JDK8%" set "JDK8=C:\Program Files\Java\jdk8"
if not exist "%JDK8%" set "JDK8=C:\Program Files\Java\jdk1.8.0"

if exist "%JDK8%\bin\javac.exe" (
    set "JAVA_HOME=%JDK8%"
    set "PATH=%JDK8%\bin;%PATH%"
) else (
    echo [AVISO] JDK 8 nao encontrado. Instale o Temurin 8 ou ajuste o caminho em build.bat.
    echo [AVISO] ajuste tambem org.gradle.java.home em gradle.properties.
)

set "PROJ=%~dp0"
set "PROJ=%PROJ:~0,-1%"

call "%~dp0gradlew.bat" -p "%PROJ%" %*
exit /b %ERRORLEVEL%
