@echo off
set PREV_DIR=%cd%

cd /d %~dp0 && java -jar imap2local.jar
cd %PREV_DIR%
