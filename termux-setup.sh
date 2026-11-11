#!/data/data/com.termux/files/usr/bin/bash
set -e
pkg update -y
pkg install -y proot-distro unzip curl nodejs-lts
termux-setup-storage
proot-distro install ubuntu || true
echo "Setup complete. For Android SDK build, see BUILD-BN.md."
