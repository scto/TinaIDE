cmake_minimum_required(VERSION 3.22.1)

project(mobile_exec_runtime C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS ON)

# Android 15+ 的 16KB 页面设备要求 ELF LOAD 段按 16KB 边界对齐。
# 这里使用全局 shared linker flags，确保 AGP/NDK 生成的实际 LINK_FLAGS 能带上该参数。
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

set(MOBILE_EXEC_VENDOR_DIR ${CMAKE_CURRENT_LIST_DIR}/../../../../vendor)
set(MOBILE_EXEC_CORE_DIR ${MOBILE_EXEC_VENDOR_DIR}/termux-core_nos_c/tre)
set(MOBILE_EXEC_LIB_DIR ${MOBILE_EXEC_VENDOR_DIR}/termux-exec_nos_c/tre)
set(MOBILE_EXEC_APP_PACKAGE "com.scto.mobileide")
set(MOBILE_EXEC_APP_DATA_DIR "/data/user/0/${MOBILE_EXEC_APP_PACKAGE}")
set(MOBILE_EXEC_ROOTFS_DIR "${MOBILE_EXEC_APP_DATA_DIR}/files")
set(MOBILE_EXEC_HOME_DIR "${MOBILE_EXEC_ROOTFS_DIR}/home")
set(MOBILE_EXEC_PREFIX_DIR "${MOBILE_EXEC_ROOTFS_DIR}/usr")
set(MOBILE_EXEC_PREFIX_INCLUDE_DIR "${MOBILE_EXEC_PREFIX_DIR}/include")
set(MOBILE_EXEC_PREFIX_LIB_DIR "${MOBILE_EXEC_PREFIX_DIR}/lib")
set(MOBILE_EXEC_PREFIX_TMP_DIR "${MOBILE_EXEC_PREFIX_DIR}/tmp")

set(MOBILE_EXEC_CORE_SOURCES
    ${MOBILE_EXEC_CORE_DIR}/src/TermuxCoreLibraryConfig.c
    ${MOBILE_EXEC_CORE_DIR}/src/android/shell/command/environment/AndroidShellEnvironment.c
    ${MOBILE_EXEC_CORE_DIR}/src/data/AssertUtils.c
    ${MOBILE_EXEC_CORE_DIR}/src/data/DataUtils.c
    ${MOBILE_EXEC_CORE_DIR}/src/logger/FileLoggerImpl.c
    ${MOBILE_EXEC_CORE_DIR}/src/logger/Logger.c
    ${MOBILE_EXEC_CORE_DIR}/src/logger/StandardLoggerImpl.c
    ${MOBILE_EXEC_CORE_DIR}/src/security/SecurityUtils.c
    ${MOBILE_EXEC_CORE_DIR}/src/termux/file/TermuxFile.c
    ${MOBILE_EXEC_CORE_DIR}/src/termux/shell/command/environment/TermuxShellEnvironment.c
    ${MOBILE_EXEC_CORE_DIR}/src/termux/shell/command/environment/termux_core/TermuxCoreShellEnvironment.c
    ${MOBILE_EXEC_CORE_DIR}/src/unix/file/Canonicalize.c
    ${MOBILE_EXEC_CORE_DIR}/src/unix/file/UnixFileUtils.c
    ${MOBILE_EXEC_CORE_DIR}/src/unix/os/process/UnixForkUtils.c
    ${MOBILE_EXEC_CORE_DIR}/src/unix/os/process/UnixSafeStrerror.c
    ${MOBILE_EXEC_CORE_DIR}/src/unix/os/process/UnixSignalUtils.c
    ${MOBILE_EXEC_CORE_DIR}/src/unix/os/selinux/UnixSeLinuxUtils.c
    ${MOBILE_EXEC_CORE_DIR}/src/unix/shell/command/environment/UnixShellEnvironment.c
)

set(MOBILE_EXEC_COMMON_SOURCES
    ${MOBILE_EXEC_LIB_DIR}/src/TermuxExecLibraryConfig.c
    ${MOBILE_EXEC_LIB_DIR}/src/termux/api/termux_exec/service/ld_preload/TermuxExecLDPreload.c
    ${MOBILE_EXEC_LIB_DIR}/src/termux/api/termux_exec/service/ld_preload/direct/exec/ExecIntercept.c
    ${MOBILE_EXEC_LIB_DIR}/src/termux/api/termux_exec/service/ld_preload/direct/exec/ExecVariantsIntercept.c
    ${MOBILE_EXEC_LIB_DIR}/src/termux/os/process/termux_exec/TermuxExecProcess.c
    ${MOBILE_EXEC_LIB_DIR}/src/termux/shell/command/environment/termux_exec/TermuxExecShellEnvironment.c
)

set(MOBILE_EXEC_COMMON_DEFINITIONS
    TERMUX_CORE_PKG__VERSION=\"2.0.0-mobile\"
    TERMUX_EXEC_PKG__VERSION=\"2.4.0-mobile\"
    TERMUX__NAME=\"MobileIDE\"
    TERMUX__LNAME=\"mobileide\"
    TERMUX__REPOS_HOST_ORG_NAME=\"Thomas Schmid\"
    TERMUX_APP__NAME=\"MobileIDE\"
    TERMUX_APP__PACKAGE_NAME=\"${MOBILE_EXEC_APP_PACKAGE}\"
    TERMUX_APP__DATA_DIR=\"${MOBILE_EXEC_APP_DATA_DIR}\"
    TERMUX__ROOTFS=\"${MOBILE_EXEC_ROOTFS_DIR}\"
    TERMUX__HOME=\"${MOBILE_EXEC_HOME_DIR}\"
    TERMUX__PREFIX=\"${MOBILE_EXEC_PREFIX_DIR}\"
    TERMUX__PREFIX__BIN_DIR=\"${MOBILE_EXEC_PREFIX_DIR}/bin\"
    TERMUX__PREFIX__INCLUDE_DIR=\"${MOBILE_EXEC_PREFIX_INCLUDE_DIR}\"
    TERMUX__PREFIX__LIB_DIR=\"${MOBILE_EXEC_PREFIX_LIB_DIR}\"
    TERMUX__PREFIX__TMP_DIR=\"${MOBILE_EXEC_PREFIX_TMP_DIR}\"
    TERMUX_ENV__S_ROOT=\"MOBILE_\"
    TERMUX_ENV__SS_TERMUX=\"\"
    TERMUX_ENV__S_TERMUX=\"MOBILE_\"
    TERMUX_ENV__SS_TERMUX_APP=\"APP__\"
    TERMUX_ENV__S_TERMUX_APP=\"MOBILE_APP__\"
    TERMUX_ENV__SS_TERMUX_ROOTFS=\"ROOTFS__\"
    TERMUX_ENV__S_TERMUX_ROOTFS=\"MOBILE_ROOTFS__\"
    TERMUX_ENV__SS_TERMUX_CORE=\"CORE__\"
    TERMUX_ENV__S_TERMUX_CORE=\"MOBILE_CORE__\"
    TERMUX_ENV__SS_TERMUX_CORE__TESTS=\"CORE__TESTS__\"
    TERMUX_ENV__S_TERMUX_CORE__TESTS=\"MOBILE_CORE__TESTS__\"
    TERMUX_ENV__SS_TERMUX_EXEC=\"EXEC__\"
    TERMUX_ENV__S_TERMUX_EXEC=\"MOBILE_EXEC__\"
    TERMUX_ENV__SS_TERMUX_EXEC__TESTS=\"EXEC__TESTS__\"
    TERMUX_ENV__S_TERMUX_EXEC__TESTS=\"MOBILE_EXEC__TESTS__\"
)

add_library(mobile_exec_vendor_config INTERFACE)
target_include_directories(mobile_exec_vendor_config INTERFACE
    ${MOBILE_EXEC_CORE_DIR}/include
    ${MOBILE_EXEC_LIB_DIR}/include
)
target_compile_definitions(mobile_exec_vendor_config INTERFACE ${MOBILE_EXEC_COMMON_DEFINITIONS})
target_compile_options(mobile_exec_vendor_config INTERFACE
    -Wall
    -Wextra
    -Werror
    -Wshadow
    -O2
    -fstack-protector-strong
)

add_library(mobile_exec_core_nos_c STATIC ${MOBILE_EXEC_CORE_SOURCES})
target_link_libraries(mobile_exec_core_nos_c PRIVATE mobile_exec_vendor_config)
set_target_properties(mobile_exec_core_nos_c PROPERTIES POSITION_INDEPENDENT_CODE ON)

add_library(mobile_exec_common_nos_c STATIC ${MOBILE_EXEC_COMMON_SOURCES})
target_link_libraries(mobile_exec_common_nos_c PRIVATE mobile_exec_vendor_config mobile_exec_core_nos_c)
set_target_properties(mobile_exec_common_nos_c PROPERTIES POSITION_INDEPENDENT_CODE ON)

add_library(mobile_exec_direct_ld_preload SHARED
    ${CMAKE_CURRENT_LIST_DIR}/entrypoints/MobileExecDirectLdPreloadEntryPoint.c
)
target_link_libraries(mobile_exec_direct_ld_preload
    PRIVATE
    mobile_exec_vendor_config
    mobile_exec_common_nos_c
    mobile_exec_core_nos_c
)
set_target_properties(mobile_exec_direct_ld_preload PROPERTIES
    OUTPUT_NAME "mobile_exec_direct_ld_preload"
)

add_library(mobile_exec_linker_ld_preload SHARED
    ${CMAKE_CURRENT_LIST_DIR}/entrypoints/MobileExecLinkerLdPreloadEntryPoint.c
)
target_link_libraries(mobile_exec_linker_ld_preload
    PRIVATE
    mobile_exec_vendor_config
    mobile_exec_common_nos_c
    mobile_exec_core_nos_c
)
set_target_properties(mobile_exec_linker_ld_preload PROPERTIES
    OUTPUT_NAME "mobile_exec_linker_ld_preload"
)
