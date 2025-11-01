#!/bin/bash

yum install -y docker #> /logs/install.log
curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose

# Start Docker (distro package)
sudo systemctl enable --now docker

# (Re)install distro plugins if you want:
sudo dnf install -y docker docker-compose-plugin || true
chmod +x /usr/local/bin/docker-compose

# Install latest Buildx + Compose as CLI plugins
PLUGIN_DIR=/usr/libexec/docker/cli-plugins
sudo mkdir -p "$PLUGIN_DIR"

ARCH="$(uname -m)"
# Buildx >= 0.17
if [ "$ARCH" = "aarch64" ]; then
  BUILDX_URL="https://github.com/docker/buildx/releases/download/v0.18.0/buildx-v0.18.0.linux-arm64"
  COMPOSE_URL="https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-aarch64"
else
  BUILDX_URL="https://github.com/docker/buildx/releases/download/v0.18.0/buildx-v0.18.0.linux-amd64"
  COMPOSE_URL="https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-x86_64"
fi

sudo curl -sSL "$BUILDX_URL" -o "$PLUGIN_DIR/docker-buildx"
sudo curl -sSL "$COMPOSE_URL" -o "$PLUGIN_DIR/docker-compose"
sudo chmod +x "$PLUGIN_DIR/docker-buildx" "$PLUGIN_DIR/docker-compose"

# Verify
docker buildx version
docker compose version