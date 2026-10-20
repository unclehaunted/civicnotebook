#!/usr/bin/env bash
# Guarda dos tokens de design (spec #5). Cada regra conta ocorrências de um
# padrão proibido e falha se a contagem passar do máximo permitido.
# Os máximos são uma catraca: só descem. Quem corrige uma regra baixa o seu
# máximo para o novo valor; o ticket indicado leva-o a 0.
set -uo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
src="$root/app/src/main/java/pt/avante/app"
fail=0

# rule <nome> <máximo> <ticket> <regex ERE> <caminho relativo a src>
rule() {
    local name="$1" max="$2" ticket="$3" pattern="$4" path="$5"
    local hits count
    hits="$(grep -rnE --include='*.kt' "$pattern" "$src/$path" || true)"
    count="$(printf '%s' "$hits" | grep -c . || true)"
    if (( count > max )); then
        echo "FALHA $name: $count ocorrências (máximo $max, ver $ticket)"
        printf '%s\n' "$hits" | sed "s|$src/||"
        fail=1
    elif (( count < max )); then
        echo "ok    $name: $count (< $max: baixar o máximo neste script)"
    else
        echo "ok    $name: $count"
    fi
}

# Cores literais na UI: vêm do resolver da Aparência (ADR 0001).
rule cores-literais 12 '#8' 'Color\(0x' ui
# Estilo sintetizado: itálico/negrito vêm de ficheiros desenhados ou do eixo wght.
rule estilo-sintetizado 1 '#6' 'setTypeface\(\s*\w+\s*,' .
# Texto abaixo de 12sp.
rule texto-abaixo-de-12sp 4 '#11' 'fontSize\s*=\s*([0-9]|1[01])(\.[0-9]+)?\.sp' .

exit "$fail"
