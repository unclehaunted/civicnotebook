#!/usr/bin/env bash
# Tudo o que o CI corre, pela mesma ordem: verde aqui é verde no CI.
# Argumentos extra vão para o Gradle (ex.: -q, --offline, --stacktrace).
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$root/scripts/check-design-tokens.sh"
cd "$root" && ./gradlew testDebugUnitTest detekt lintDebug "$@"
