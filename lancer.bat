@echo off
REM ============================================================
REM  Lancer le Systeme Bancaire ASJA (Windows)
REM  Prerequis : JDK 11 ou plus (javac et java dans le PATH)
REM ============================================================
chcp 65001 >nul
cd /d "%~dp0"
echo Compilation du projet...
if exist bin rmdir /s /q bin
mkdir bin
javac -encoding UTF-8 -d bin -sourcepath src src\Lancer.java
if errorlevel 1 (
    echo.
    echo ERREUR de compilation. Verifiez que le JDK 11 est installe : javac -version
    pause
    exit /b 1
)
echo Lancement de l'application...
java -Dfile.encoding=UTF-8 -cp bin Lancer %*
