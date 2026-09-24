@echo off
setlocal

pushd "%~dp0"
call mvn javadoc:javadoc
set "exitCode=%ERRORLEVEL%"
popd

if not "%exitCode%" == "0" (
    echo Javadoc generation failed with exit code %exitCode%.
    exit /b %exitCode%
)

echo Javadoc generated at target\reports\apidocs\index.html
exit /b 0