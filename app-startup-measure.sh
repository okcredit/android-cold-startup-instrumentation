#!/bin/zsh

CUMULATIVE_TIME=0
LOOP_COUNT=0
PACKAGE_NAME="tech.okcredit.appstartupinstrumentation"
MAIN_ACTIVITY="tech.okcredit.appstartupinstrumentation.NavigationActivity"
getLaunchTime() {
  adb shell am start-activity -W -n $PACKAGE_NAME/$MAIN_ACTIVITY | grep "TotalTime" | cut -d ' ' -f 2
}

echo ">> Test start <<"

for i in $(seq 1 25); do
  LOOP_COUNT=$((LOOP_COUNT + 1))

  adb shell am force-stop $PACKAGE_NAME
  sleep 1

  THIS_LAUNCH_TIME=$(getLaunchTime)
  CUMULATIVE_TIME=$((CUMULATIVE_TIME + THIS_LAUNCH_TIME))

  echo -n "."
done

printf "\n>> Test end <<\n"
echo "Average startup time: $((CUMULATIVE_TIME / LOOP_COUNT))ms"