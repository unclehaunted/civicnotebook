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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.avante.app.data.ReaderFont
import pt.avante.app.data.ReaderFontSize
import pt.avante.app.data.ReaderSettingsStore
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.auth.AuthState
import pt.avante.app.data.auth.AvanteAuthRepository
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.SoftDivider
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.TextSecondary
import pt.avante.app.theme.WarmPaper
import pt.avante.app.theme.readerFontFamily
import pt.avante.app.theme.Spacing

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

    Surface(modifier = Modifier.fillMaxSize(), color = WarmPaper) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xxl, start = Spacing.xs, end = Spacing.m, bottom = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = OriginalRed
                    )
                }
                Text(
                    text = "Definições",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.l)
                    .padding(bottom = Spacing.xxl)
            ) {
                // ==================== LEITURA ====================
                SectionHeader("Leitura")

                // Preview ao vivo
                Surface(
                    color = Color(settings.theme.backgroundColor),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, SoftDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.m)) {
                        Text(
                            text = "Proletários de todos os países, uni-vos!",
                            fontFamily = readerFontFamily(settings.font),
                            fontWeight = FontWeight.Bold,
                            fontSize = settings.fontSize.spValue.sp,
                            color = Color(settings.theme.textColor)
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "O Avante! é o Órgão Central do Partido Comunista Português. " +
                                    "Assim se apresenta, semanalmente, aos seus leitores desde 1931.",
                            fontFamily = readerFontFamily(settings.font),
                            fontSize = settings.fontSize.spValue.sp,
                            lineHeight = (settings.fontSize.spValue * 1.4f).sp,
                            color = Color(settings.theme.textColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.l))

                // Tipo de letra
                SettingLabel("Tipo de letra")
                ReaderFont.entries.forEach { font ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settingsStore.setFont(font) }
                            .padding(vertical = Spacing.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.font == font,
                            onClick = { settingsStore.setFont(font) },
                            colors = RadioButtonDefaults.colors(selectedColor = OriginalRed)
                        )
                        Text(
                            text = font.label,
                            fontFamily = readerFontFamily(font),
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                    }
                }

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
                                activeContainerColor = OriginalRed.copy(alpha = 0.1f),
                                activeContentColor = OriginalRed,
                                inactiveContentColor = TextSecondary
                            )
                        ) {
                            Text(size.label, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // Cor de fundo
                SettingLabel("Cor de fundo")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    ReaderTheme.entries.forEach { theme ->
                        val selected = settings.theme == theme
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { settingsStore.setTheme(theme) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(theme.backgroundColor), CircleShape)
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) OriginalRed else SoftDivider,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (theme == ReaderTheme.DARK) Color.White else OriginalRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.xxs))
                            Text(
                                text = theme.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) OriginalRed else TextSecondary
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
                            color = TextPrimary
                        )
                        Text(
                            text = "Nos artigos e na lista da edição",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = settings.showImages,
                        onCheckedChange = { settingsStore.setShowImages(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = OriginalRed,
                            checkedThumbColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                HorizontalDivider(color = SoftDivider)
                Spacer(modifier = Modifier.height(Spacing.l))

                // ==================== ACESSO AO ARQUIVO ====================
                SectionHeader("Acesso ao arquivo")

                Text(
                    text = "A edição impressa do Avante! publica semanalmente uma palavra-passe " +
                            "que dá acesso à edição digital e a todo o arquivo até ao número seguinte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(Spacing.m))

                when (authState) {
                    AuthState.LOGGED_IN -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF2F4F46), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "Acesso ativo",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF2F4F46),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Se os artigos voltarem a aparecer bloqueados, introduza a nova " +
                                    "palavra-passe publicada no número mais recente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(Spacing.m))
                        OutlinedButton(
                            onClick = { authRepository.logout() },
                            border = BorderStroke(1.dp, OriginalRed)
                        ) {
                            Text("Desativar acesso", color = OriginalRed)
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OriginalRed,
                                focusedLabelColor = OriginalRed,
                                cursorColor = OriginalRed
                            )
                        )

                        if (authState == AuthState.PASSWORD_REJECTED) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Palavra-passe recusada. Confirme que está a usar a publicada " +
                                        "no número mais recente do jornal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OriginalRed
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
                            colors = ButtonDefaults.buttonColors(containerColor = OriginalRed),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loginInProgress) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Ativar acesso", color = Color.White)
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
        color = OriginalRed,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.s)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
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
        color = TextSecondary,
        modifier = Modifier.padding(bottom = Spacing.xs)
    )
}
