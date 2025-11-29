@echo off
set JAVA_HOME="C:\Program Files\Java\jdk-17.0.10+7"
set PATH=%JAVA_HOME%\bin;%PATH%
"C:\Program Files\Gradle\gradle-8.10\bin\gradle.bat" assembleDebug
