# Native CMake / Ninja linker64 排障记录

## 现象

在 Android 9 / x86_64 模拟器上，MobileIDE 内部构建 CMake 项目时，CMake configure 阶段可能失败：

```text
The detected version of Ninja (This is /system/bin/linker64, the helper
program for dynamic executables.) is less than the version of Ninja required by CMake (1.3).
```

典型触发链路：

```text
App 内 CMake configure
  -> CMake 二次执行 ninja --version
  -> Android 9 / 部分模拟器返回 linker64 helper 文本
  -> CMake 将该文本当作 Ninja 版本
  -> configure 失败
```

## 影响范围

- 已确认环境：逍遥模拟器，Android 9 / SDK 28，x86_64。
- 风险环境：Android 9 及部分模拟器 ROM。
- Android 10+ 主路径不应受该问题影响；该路径仍优先保持原有真实构建工具执行方式。

该问题不是 APK 没重新编译，也不是 `ninja` 二进制损坏。直接在设备 shell 里执行 `ninja --version` 可能正常，但 CMake 从 App 私有目录二次启动 `ninja` 时仍可能拿到 linker64 helper 文本。

## 当前修复

修复位于：

- `NativeCMakeBuildExecutor.kt`
- `ToolchainLinker64ShimManager.kt`

核心策略：

1. 当 `NativeExecutableRunner.shouldPreferLinker64()` 为 `false` 时，为 CMake 构建工具生成 direct shell shim：

   ```text
   /data/user/0/<package>/files/toolchain-direct-shims/<hash>/bin/ninja
   ```

2. CMake configure 阶段将 `CMAKE_MAKE_PROGRAM` 指向 direct shell shim：

   ```text
   -DCMAKE_MAKE_PROGRAM=/data/user/0/.../toolchain-direct-shims/.../bin/ninja
   ```

3. direct shell shim 只做一件事：

   ```sh
   exec "$REAL_BIN" "$@"
   ```

4. 如果 direct shell shim configure 失败，则只在该失败路径回退真实 `ninja` 并重试一次，避免部分 Android 9 真机 ROM 禁止 App 私有目录 shell 脚本执行时直接失败。

5. 当 `preferLinker64=true` 时，不把 linker64 shell shim 传给 `CMAKE_MAKE_PROGRAM`，仍保持真实构建工具路径，避免 CMake 无法处理带参数的 shell 启动形式。

## 验证命令

本地最小编译验证：

```powershell
.\gradlew.bat :core:compile:compileDebugKotlin --console=plain
.\gradlew.bat :app:compileArm64DebugKotlin --console=plain
```

x86_64 模拟器构建安装：

```powershell
.\tools\build-apk.ps1 -Abi x86 -Install
```

安装后在设备上触发项目构建，再抓取关键日志：

```powershell
.\tools\device-native-smoke.ps1 -AdbPath "D:\Program Files\Microvirt\MEmu\adb.exe" -Serial 127.0.0.1:21503 -TapBuild
```

## 期望日志

成功路径应看到：

```text
Resolved build tool: generator=Ninja, buildTool=ninja, preferLinker64=false, buildToolSource=shim
CMAKE_MAKE_PROGRAM=/data/user/0/.../toolchain-direct-shims/.../bin/ninja
-- Configuring done
-- Generating done
Build finished: report=BuiltOnly
Compile built-only success
```

不应再出现：

```text
The detected version of Ninja (This is /system/bin/linker64...
```

## 设备验证矩阵

建议每次修改 native CMake / toolchain 执行链路后至少覆盖：

- Android 9 x86_64 模拟器：验证 linker64 / Ninja 检测问题。
- Android 10+ x86_64 模拟器：验证新系统模拟器主路径。
- Android 11+ arm64 真机：验证主流真实设备路径。
- Android 13/14/15 arm64 真机：验证较新系统执行策略。

每台设备最少确认：

```text
设备 ABI / Android SDK
CMAKE_MAKE_PROGRAM
是否出现 linker64 helper 文本
CMake configure 是否成功
Ninja build 是否成功
构建产物是否导出
```

## 排障提示

如果仍然失败，优先检查日志中的这几项：

- `Resolved build tool`
- `CMAKE_MAKE_PROGRAM`
- `Native exec diag`
- `Toolchain shim prepare`
- `CMake configure failure summary`
- `The detected version of Ninja`
- `linker64`

判断方式：

- `CMAKE_MAKE_PROGRAM` 仍指向真实 `ninja`，且出现 linker64 helper 文本：direct shim 没有被选中，检查 `prepareDirectShell()` 是否生成成功。
- `CMAKE_MAKE_PROGRAM` 指向 `toolchain-direct-shims`，但 configure 失败：检查 shell shim 是否可执行，或者是否触发了回退真实 `ninja`。
- configure 成功但 build 失败：问题已经不在 Ninja 版本检测，继续看 clang/lld/sysroot/项目 CMakeLists。
