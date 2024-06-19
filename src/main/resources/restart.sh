#!/bin/sh
PID=$(ps -ef | grep notification | grep -v grep | awk '{print $2}')
echo '=============PID==='$PID
kill -9 $PID
echo '========successfully killed process====='
nohup java -jar notification-0.0.1-SNAPSHOT.jar &
echo '========successfully start process====='