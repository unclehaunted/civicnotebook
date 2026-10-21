#!/usr/bin/env bash
# Guarda dos tokens de design (spec #5). Cada regra conta ocorrências de um
# padrão proibido e falha se a contagem passar do máximo permitido.
# Os máximos são uma catraca: só descem. Quem corrige uma regra baixa o seu
# máximo para o novo valor; o ticket indicado leva-o a 0.
set -uo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
src="$root/app/src/main/java/pt/avante/app"
fail=0

# rule <nome> <máximo> <ticket> <regex ERE> <caminho relativo a src> [ficheiro a ignorar] [pasta a ignorar]
rule() {
    local name="$1" max="$2" ticket="$3" pattern="$4" path="$5" exclude="${6:-}" exclude_dir="${7:-}"
    local hits count
    hits="$(grep -rnE --include='*.kt' ${exclude:+--exclude="$exclude"} ${exclude_dir:+--exclude-dir="$exclude_dir"} "$pattern" "$src/$path" || true)"
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
rule cores-literais 11 '#8' 'Color\(0x' ui
# Estilo sintetizado: itálico/negrito vêm de ficheiros desenhados ou do eixo wght.
rule estilo-sintetizado 1 '#6' 'setTypeface\(\s*\w+\s*,' .
# Texto abaixo de 12sp.
rule texto-abaixo-de-12sp 4 '#11' 'fontSize\s*=\s*([0-9]|1[01])(\.[0-9]+)?\.sp' .

# O ecrã do Arquivo é maqueta (#35, #40): fica fora das regras de layout.
maqueta='ArquivoScreen.kt'
# Espaçamento com literal dp: vem do objeto Spacing do tema (spec #49).
# Conta chamadas padding/PaddingValues/spacedBy/Spacer com um literal dentro
# (até dois níveis de parênteses), argumentos nomeados de padding numa linha
# própria e atribuições a nomes de espaçamento (…Padding, …Spacing, …Gap).
# Tamanhos (size, height, width fora de Spacer) não são espaçamento.
espaco_chamada='(padding|PaddingValues|spacedBy|Spacer)\(([^()]|\([^()]*\))*(\(([^()]|\([^()]*\))*)?[0-9]\.dp'
espaco_nomeado='^\s*(horizontal|vertical|top|bottom|start|end|all)\s*=\s*[^,]*[0-9]\.dp'
espaco_atribuido='([sS]pacing|[pP]adding|Gap)\s*=.*[0-9]\.dp'
rule espacamento-literal 0 '#50' "$espaco_chamada|$espaco_nomeado|$espaco_atribuido" ui "$maqueta"
# Raios com literal dp: as formas vêm de MaterialTheme.shapes (theme/Shape.kt).
rule raio-literal 0 '#50' 'RoundedCornerShape\(([^()]|\([^()]*\))*[0-9]\.dp' ui "$maqueta"
# Margem superior fixa no lugar dos insets: a margem de um cabeçalho vem da
# barra de estado (TitleBar e o padding do Scaffold), nunca de um valor fixo.
# Conta `top =` num padding/PaddingValues com um literal dp ou um degrau do
# tamanho de uma barra de estado (Spacing.l/xl/xxl, 24–48dp); os degraus
# menores são ritmo entre elementos e não substituem insets.
# O leitor fica de fora: não tem barra de título (não é um dos seis ecrãs da
# spec #49) e já está abaixo da barra de estado pelo padding do Scaffold; as
# margens de ReaderHero/ReaderFloatingActions/ReaderBody afastam os botões
# flutuantes e o título sobre a imagem, não a barra de estado.
margem_valor='([0-9]+(\.[0-9]+)?\.dp|Spacing\.(l|xl|xxl)\b)'
margem_chamada="(padding|PaddingValues)\(([^()]*,\s*)?top\s*=\s*$margem_valor"
margem_nomeada="^\s*top\s*=\s*$margem_valor"
rule margem-superior-fixa 0 '#51' "$margem_chamada|$margem_nomeada" ui '' reader

exit "$fail"
