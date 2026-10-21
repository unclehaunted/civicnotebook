package pt.avante.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.avante.app.data.ReaderFontSize
import pt.avante.app.data.ReaderSettingsStore
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.auth.AuthState
import pt.avante.app.data.auth.AvanteAuthRepository
import pt.avante.app.data.paleta
import pt.avante.app.theme.Cores
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.emMaiusculas
import pt.avante.app.theme.tipografiaDoArtigo
import pt.avante.app.ui.components.TitleBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinicoesScreen(
    settingsStore: ReaderSettingsStore,
    authRepository: AvanteAuthRepository,
    onBackClick: () -> Unit
) {
    val settings by settingsStore.settings.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val loginInProgress by authRepository.loginInProgress.collectAsState()
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = Cores.fundo) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Definições", onBack = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.l)
                    .padding(bottom = Spacing.xxl)
            ) {
                // ==================== LEITURA ====================
                SectionHeader("Leitura")

                // Preview ao vivo (a app inteira já está na Aparência escolhida)
                Surface(
                    color = Cores.fundo,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, Cores.divisor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Tamanho, Tipos de letra e Espaçamento como no artigo: dois parágrafos para
                    // se ver a entrelinha e o espaço entre eles.
                    val tipografia = tipografiaDoArtigo(settings)
                    val paragrafo = with(LocalDensity.current) { tipografia.espacamento.paragraphSp.sp.toDp() }
                    val corpo = tipografia.corpo.copy(color = Cores.textoPrimario)
                    Column(modifier = Modifier.padding(Spacing.m)) {
                        Text(
                            text = "Proletários de todos os países, uni-vos!",
                            style = tipografia.subtitulo,
                            color = Cores.textoPrimario
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "O Avante! é o Órgão Central do Partido Comunista Português.",
                            style = corpo
                        )
                        Spacer(modifier = Modifier.height(paragrafo))
                        Text(
                            text = "Assim se apresenta, semanalmente, aos seus leitores desde 1931.",
                            style = corpo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.l))

                SettingLabel("Tipos de letra")
                TiposDeLetraOpcoes(settings.tiposDeLetra, settingsStore::setTiposDeLetra)

                Spacer(modifier = Modifier.height(Spacing.m))

                SettingLabel("Escrita")
                Text(
                    text = "Para o que escreve nas suas Notas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Cores.textoSecundario
                )
                EscritaOpcoes(settings.escrita, settingsStore::setEscrita)

                Spacer(modifier = Modifier.height(Spacing.m))

                // Tamanho do texto
                SettingLabel("Tamanho do texto")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ReaderFontSize.entries.forEachIndexed { index, size ->
                        SegmentedButton(
                            selected = settings.fontSize == size,
                            onClick = { settingsStore.setFontSize(size) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ReaderFontSize.entries.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Cores.acento.copy(alpha = 0.1f),
                                activeContentColor = Cores.acento,
                                inactiveContentColor = Cores.textoSecundario
                            )
                        ) {
                            Text(size.label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                SettingLabel("Espaçamento")
                EspacamentoOpcoes(settings.spacing, settingsStore::setSpacing)

                Spacer(modifier = Modifier.height(Spacing.m))

                // Cor de fundo
                SettingLabel("Cor de fundo")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    ReaderTheme.entries.forEach { theme ->
                        val selected = settings.theme == theme
                        // Cada amostra mostra o fundo e o acento da sua própria Aparência.
                        val amostra = paleta(theme)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { settingsStore.setTheme(theme) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(amostra.fundo), CircleShape)
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) Cores.acento else Cores.divisor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(amostra.acento),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.xxs))
                            Text(
                                text = theme.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Cores.acento else Cores.textoSecundario
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // Imagens
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mostrar imagens",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Cores.textoPrimario
                        )
                        Text(
                            text = "Nos artigos e na lista da edição",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cores.textoSecundario
                        )
                    }
                    Switch(
                        checked = settings.showImages,
                        onCheckedChange = { settingsStore.setShowImages(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Cores.acento,
                            checkedThumbColor = Cores.sobreAcento
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                HorizontalDivider(color = Cores.divisor)
                Spacer(modifier = Modifier.height(Spacing.l))

                // ==================== ACESSO AO ARQUIVO ====================
                SectionHeader("Acesso ao arquivo")

                Text(
                    text = "A edição impressa do Avante! publica semanalmente uma palavra-passe " +
                            "que dá acesso à edição digital e a todo o arquivo até ao número seguinte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cores.textoSecundario
                )

                Spacer(modifier = Modifier.height(Spacing.m))

                when (authState) {
                    AuthState.LOGGED_IN -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Cores.acento, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "Acesso ativo",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Cores.textoPrimario,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Se os artigos voltarem a aparecer bloqueados, introduza a nova " +
                                    "palavra-passe publicada no número mais recente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cores.textoSecundario
                        )
                        Spacer(modifier = Modifier.height(Spacing.m))
                        OutlinedButton(
                            onClick = { authRepository.logout() },
                            border = BorderStroke(1.dp, Cores.acento)
                        ) {
                            Text("Desativar acesso", color = Cores.acento)
                        }
                    }

                    AuthState.LOGGED_OUT, AuthState.PASSWORD_REJECTED -> {
                        var password by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Palavra-passe da semana") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = authState == AuthState.PASSWORD_REJECTED,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cores.acento,
                                focusedLabelColor = Cores.acento,
                                cursorColor = Cores.acento
                            )
                        )

                        if (authState == AuthState.PASSWORD_REJECTED) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Palavra-passe recusada. Confirme que está a usar a publicada " +
                                        "no número mais recente do jornal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Cores.acento
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.m))

                        Button(
                            onClick = {
                                scope.launch {
                                    val ok = authRepository.loginWithWeeklyPassword(password)
                                    if (ok) password = ""
                                }
                            },
                            enabled = password.isNotBlank() && !loginInProgress,
                            colors = ButtonDefaults.buttonColors(containerColor = Cores.acento),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loginInProgress) {
                                CircularProgressIndicator(
                                    color = Cores.sobreAcento,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Ativar acesso", color = Cores.sobreAcento)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Surface(
        color = Cores.acento,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.s)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge.emMaiusculas(),
            color = Cores.sobreAcento,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs)
        )
    }
}

@Composable
private fun SettingLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = Cores.textoSecundario,
        modifier = Modifier.padding(bottom = Spacing.xs)
    )
}
