# run_new_was.sh

#!/bin/bash

CURRENT_PORT=$(cat /home/ec2-user/service_url.inc | grep -Po '[0-9]+' | tail -1)
TARGET_PORT=0


PROJECT_ROOT="/home/ec2-user/app"
JAR_FILE="$PROJECT_ROOT/spring-app.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# build 파일 복사
echo "$TIME_NOW > $JAR_FILE 파일 복사" >> $DEPLOY_LOG
cp $PROJECT_ROOT/build/libs/*.jar $JAR_FILE

echo "> Current port of running WAS is ${CURRENT_PORT}."

if [ ${CURRENT_PORT} -eq 8081 ]; then
  TARGET_PORT=8082
  APP_LOG="$PROJECT_ROOT/application_8082.log"
elif [ ${CURRENT_PORT} -eq 8082 ]; then
  TARGET_PORT=8081
  APP_LOG="$PROJECT_ROOT/application_8081.log"
else
  echo "> No WAS is connected to nginx"
fi

TARGET_PID=$(lsof -Fp -i TCP:${TARGET_PORT} | grep -Po 'p[0-9]+' | grep -Po '[0-9]+')

if [ ! -z ${TARGET_PID} ]; then
  echo "> Kill WAS running at ${TARGET_PORT}."
  sudo kill -15 ${TARGET_PID}
fi

echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java -jar -Dspring.config.location=classpath:application.properties,/home/ec2-user/app/application-aws.properties \
                -Dserver.port=${TARGET_PORT} $JAR_FILE > $APP_LOG 2> $ERROR_LOG &
echo "> Now new WAS runs at ${TARGET_PORT}."
exit 0