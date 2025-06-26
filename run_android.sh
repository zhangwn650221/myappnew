#!/bin/bash
# 启动模拟器（如已启动会自动跳过）
emulator -avd Pixel_9_Pro_XL -netdelay none -netspeed full -no-snapshot-load &
# 等待模拟器启动完成
adb wait-for-device
# 构建 APK（假设用 gradlew）
./gradlew assembleDebug
# 安装 APK 到模拟器
adb install -r app/build/outputs/apk/debug/app-debug.apk
# 打开 app（包名需替换为你的实际包名）
adb shell monkey -p com.example.myappnew -c android.intent.category.LAUNCHER 1
