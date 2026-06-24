@echo off
set "JAVA_HOME=D:\java project\Software-Engineering--The-Imitation-of-Weibo\.jdk\jdk-21.0.8"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "D:\java project\Software-Engineering--The-Imitation-of-Weibo"
java -jar "target\Software-Engineering-0.0.1-SNAPSHOT.jar" > "app-run.log" 2>&1