# MobileIDE Toolchain & Git LFS Guide

This guide describes how to handle the Git LFS quota issue and how the compilation/packaging process works for `mobile-toolchain` assets.

---

## 1. The SHA256 Mismatch & Git LFS Issue

When cloning the repository or pulling changes, you might get a verification failure because the toolchain files are tracked via **Git LFS**. If GitHub's LFS transfer bandwidth for the repository is exceeded, Git downloads the text pointer files instead of the large compressed tar archives.

* **Expected SHA256:** `02cd6c313c682160d833ddd8009c6633adca504feadbb376ad774470d021fcac` (the real toolchain archive).
* **Actual SHA256:** `065cb97b12207a932944c154a816d6f56b7a20b90bcd2ed7b1b538318c821ff6` (the text pointer file).

### Fix for Git LFS Quota Limits
1. Contact repository owner to increase LFS bandwidth.
2. Manually download the assets from the GitHub Release page if the maintainer has uploaded them there.
3. Drop the downloaded archives directly into:
   - `app/src/arm64/assets/mobile-toolchain/`
   - `app/src/x86_64/assets/mobile-toolchain/`

---

## 2. Bypassing Gradle Build Checks with Dummy Assets

If you only want to compile/assemble the APK and don't need the working compiler on your target run, you can create dummy files and matching hashes.

Execute the following commands in the project root:

```bash
# Create dummy file and hash for arm64
echo "dummy" > app/src/arm64/assets/mobile-toolchain/mobileide-toolchain-aarch64-v0.2.4-patched.tar.xz
HASH_ARM=$(sha256sum app/src/arm64/assets/mobile-toolchain/mobileide-toolchain-aarch64-v0.2.4-patched.tar.xz | cut -d' ' -f1)
echo "${HASH_ARM}  mobileide-toolchain-aarch64-v0.2.4-patched.tar.xz" > app/src/arm64/assets/mobile-toolchain/mobileide-toolchain-aarch64-v0.2.4-patched.sha256

# Create dummy file and hash for x86_64
echo "dummy" > app/src/x86_64/assets/mobile-toolchain/mobileide-toolchain-x86_64-v0.2.4-patched.tar.xz
HASH_X86=$(sha256sum app/src/x86_64/assets/mobile-toolchain/mobileide-toolchain-x86_64-v0.2.4-patched.tar.xz | cut -d' ' -f1)
echo "${HASH_X86}  mobileide-toolchain-x86_64-v0.2.4-patched.tar.xz" > app/src/x86_64/assets/mobile-toolchain/mobileide-toolchain-x86_64-v0.2.4-patched.sha256
```

---

## 3. How to Pack and Publish Official Release Assets

If you want to package the official toolchain yourself to upload it to GitHub Releases, do the following:

### Execution Command
Run the toolchain builder script using Docker:
```powershell
pwsh -File tools/run-toolchain-builder.ps1 -Command "cd /workspace && bash scripts/build-and-package-android-toolchain.sh"
```

### Under the Hood
The build script performs the following tasks:
1. Copies compiled tools to the staging directory.
2. Removes `android-sysroot` (packaged as a separate asset).
3. Compresses the directory using `xz` with high compression `-9e`:
   ```bash
   tar -cf - "mobileide-toolchain-aarch64-v0.2.4-patched" | xz -9e -T0 > "mobileide-toolchain-aarch64-v0.2.4-patched.tar.xz"
   ```
4. Generates the sha256 checksum file:
   ```bash
   sha256sum "mobileide-toolchain-aarch64-v0.2.4-patched.tar.xz" > "mobileide-toolchain-aarch64-v0.2.4-patched.sha256"
   ```

These assets are generated in `build/mobile-toolchain/release/` and are ready for upload to GitHub Releases.
