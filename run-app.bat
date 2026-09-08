@echo off
rem 本地运行脚本:在项目根目录使用系统 Java 启动已打包的 jar
cd /d "%~dp0"
java -jar "target\Software-Engineering-0.0.1-SNAPSHOT.jar" > "app-run.log" 2>&1
