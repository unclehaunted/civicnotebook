#!/usr/bin/env bash
# Corre os testes unitários e imprime uma única linha de resumo:
#   tests N fail F err E
# seguida dos nomes dos casos que falharam. Argumentos extra vão para o Gradle
# (ex.: --offline, --tests '*ArticleBlockSerializerTest').
# Sai com código != 0 se o build ou algum teste falhar.
set -uo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
app="$root"
results="$app/app/build/test-results/testDebugUnitTest"
log="$(mktemp)"
trap 'rm -f "$log"' EXIT

# Evita somar resultados antigos se o build falhar antes de correr os testes.
rm -rf "$results"

(cd "$app" && ./gradlew testDebugUnitTest "$@") >"$log" 2>&1
gradle_rc=$?

shopt -s nullglob
xmls=("$results"/*.xml)

tests=0 fail=0 err=0
failed_cases=""
if ((${#xmls[@]})); then
  read -r tests fail err < <(
    grep -h '^<testsuite ' "${xmls[@]}" | sed -E \
      's/.* tests="([0-9]+)".* failures="([0-9]+)" errors="([0-9]+)".*/\1 \2 \3/' |
      awk '{t+=$1; f+=$2; e+=$3} END {print t+0, f+0, e+0}'
  )
  failed_cases="$(
    awk '
      /<testcase / {
        match($0, /classname="[^"]*"/); cls = substr($0, RSTART + 11, RLENGTH - 12)
        match($0, / name="[^"]*"/);     nm  = substr($0, RSTART + 7,  RLENGTH - 8)
        cur = cls "." nm
      }
      /<failure|<error/ { if (cur != "") { print cur; cur = "" } }
    ' "${xmls[@]}"
  )"
fi

echo "tests $tests fail $fail err $err"
[[ -n "$failed_cases" ]] && echo "$failed_cases"

if ((gradle_rc != 0)); then
  # Falha sem testes falhados (compilação, dependências, ...): mostra o fim do log.
  if ((fail == 0 && err == 0)); then
    echo "build falhou (gradle rc=$gradle_rc):" >&2
    tail -n 25 "$log" >&2
  fi
  exit "$gradle_rc"
fi
((fail == 0 && err == 0)) || exit 1
