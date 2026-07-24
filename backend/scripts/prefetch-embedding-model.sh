#!/usr/bin/env bash
# Pre-downloads the local embedding model (all-MiniLM-L6-v2 tokenizer + ONNX
# weights) into the persistent cache directory configured in
# application.properties (spring.ai.embedding.transformer.cache.directory).
#
# Without this, the ~90MB model.onnx is fetched from GitHub the first time the
# app starts on a machine - fine normally, but risky right before a live demo
# on limited bandwidth. Run this once ahead of time (e.g. the night before)
# so demo day startup never touches the network for the embedding model.
#
# The subfolder names below are not arbitrary: Spring AI derives them as
# UUID.nameUUIDFromBytes(<url without the final path segment>), so they must
# match exactly for the app to recognize these files as already cached.
set -euo pipefail

CACHE_DIR="$HOME/.cache/spring-ai-onnx-model"

TOKENIZER_DIR="$CACHE_DIR/4d42ba07-cb22-352f-bb44-beccc8c8c0b7"
TOKENIZER_URL="https://raw.githubusercontent.com/spring-projects/spring-ai/main/models/spring-ai-transformers/src/main/resources/onnx/all-MiniLM-L6-v2/tokenizer.json"

MODEL_DIR="$CACHE_DIR/d5769837-23ea-3f7c-a3e3-ded3412df4f4"
MODEL_URL="https://media.githubusercontent.com/media/spring-projects/spring-ai/refs/heads/main/models/spring-ai-transformers/src/main/resources/onnx/all-MiniLM-L6-v2/model.onnx"

fetch() {
  local dest_dir="$1" url="$2" name="$3"
  mkdir -p "$dest_dir"
  if [ -s "$dest_dir/$name" ]; then
    echo "Already cached: $dest_dir/$name"
  else
    echo "Downloading $name..."
    curl -fL --progress-bar -o "$dest_dir/$name" "$url"
  fi
}

fetch "$TOKENIZER_DIR" "$TOKENIZER_URL" "tokenizer.json"
fetch "$MODEL_DIR" "$MODEL_URL" "model.onnx"

echo "Embedding model cache ready at $CACHE_DIR"