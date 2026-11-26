@echo off
title Missao Marte
cd /d "%~dp0fase-07-evolucao-livre\cliente"
echo Abrindo o Missao Marte... (a primeira vez demora alguns minutos)
call gradlew.bat :lwjgl3:run
if errorlevel 1 pause
