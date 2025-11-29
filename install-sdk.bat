@echo off
set JAVA_HOME="C:\Program Files\Java\jdk-17.0.10+7"
set PATH=%JAVA_HOME%\bin;%PATH%
echo y | "C:\Android\cmdline-tools\bin\sdkmanager.bat" --install platform-tools build-tools;34.0.0 platforms;android-34
