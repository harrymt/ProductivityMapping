#!/bin/sh

# Navigate to the adb directory
cd /Users/harrymt/Library/Android/sdk/platform-tools

# Runs the adb shell
# Runs the productivitymapping app
# Navigates to the database directory and pulls the userData database to the desktop
./adb exec-out run-as com.harrymt.productivitymapping cat databases/userData > ~/Desktop/g53ids-userData-database