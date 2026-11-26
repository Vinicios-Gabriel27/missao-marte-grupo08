@echo off
title Missao Marte - servidor + jogo
echo Iniciando o servidor de ranking em outra janela...
start "Servidor Missao Marte" cmd /k "cd /d "%~dp0fase-07-evolucao-livre\servidor" && gradlew.bat bootRun"
echo Aguardando o servidor subir...
timeout /t 25 /nobreak >nul
cd /d "%~dp0fase-07-evolucao-livre\cliente"
call gradlew.bat :lwjgl3:run
if errorlevel 1 pause
