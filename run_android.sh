#!/bin/bash
set -e

# 彩色输出函数
green() { echo -e "\033[32m$1\033[0m"; }
red() { echo -e "\033[31m$1\033[0m"; }
yellow() { echo -e "\033[33m$1\033[0m"; }

# 可选参数：AVD 名称、APK 路径
default_avd="Pixel_9_Pro_XL"
avd_name="${1:-$default_avd}"
apk_path="${2:-app/build/outputs/apk/debug/app-debug.apk}"

# 检查依赖
command -v emulator >/dev/null 2>&1 || { red "emulator 未安装或未在 PATH 中"; exit 1; }
command -v adb >/dev/null 2>&1 || { red "adb 未安装或未在 PATH 中"; exit 1; }
[ -f ./gradlew ] || { red "gradlew 未找到，请在项目根目录运行"; exit 1; }

# 检查模拟器是否已启动
if adb devices | grep -w emulator >/dev/null; then
  green "模拟器已启动，跳过启动步骤"
else
  yellow "启动模拟器: $avd_name ..."
  # 增大模拟器内存（如 4096MB），如需更大可调整 --memory
  emulator -avd "$avd_name" -memory 4096 -netdelay none -netspeed full -no-snapshot-load &
  sleep 2
fi

yellow "等待模拟器启动..."
adb wait-for-device

green "开始构建 APK..."
./gradlew assembleDebug

if [ ! -f "$apk_path" ]; then
  red "APK 构建失败: $apk_path 未找到"; exit 2
fi

green "安装 APK 到模拟器..."
adb install -r "$apk_path"

# 自动获取包名（优先 AndroidManifest.xml）
if [ -f app/src/main/AndroidManifest.xml ]; then
  pkg=$(grep -oP 'package="\K[^"]+' app/src/main/AndroidManifest.xml | head -1)
else
  pkg="com.example.myappnew"
fi

green "启动 App: $pkg ..."
adb shell monkey -p "$pkg" -c android.intent.category.LAUNCHER 1

green "全部完成！你可以在模拟器中体验最新构建的 App。"
