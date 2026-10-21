package pt.avante.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.Cores
import pt.avante.app.ui.components.TitleBar

// Placeholder mock. The subscriber gate below is mock-only: the Arquivo is open to all
// and its behaviour is being decided in GitHub issue #35 (map).
@Composable
fun ArquivoScreen(
    isSubscriber: Boolean = false, // Mock for now
    isOnline: Boolean = false // Mock for now
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cores.fundo
    ) {
        pt.avante.app.ui.layout.AdaptiveContent(maxWidth = 960.dp) {
          Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Arquivo")
            if (!isSubscriber) {
                // Non-subscriber view: Blurred grid + Red pill
                Box(modifier = Modifier.fillMaxSize()) {
                    // Mock Grid (Blurred)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(12.dp)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(4) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(2) {
                                    Box(
                                        modifier = Modifier.weight(1f).aspectRatio(0.7f)
                                            .background(Cores.divisor, RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }
    
                    // Overlay CTA
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Cores.acento,
                            shape = RoundedCornerShape(32.dp),
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                "Acesso ao Arquivo é exclusivo para assinantes.",
                                color = Cores.sobreAcento,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { /* TODO: Subscribe */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Cores.textoPrimario),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Assinar o Avante!", color = Cores.fundo)
                        }
                    }
                }
            } else if (!isOnline) {
                // Subscriber but offline
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = "Offline",
                            tint = Cores.textoSecundario,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Não é possível aceder ao arquivo sem rede de internet disponível.",
                            color = Cores.textoPrimario,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                // Subscriber and Online (Actual Archive View)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Arquivo Completo Disponível (Brevemente)", color = Cores.textoPrimario)
                }
            }
          }
        }
    }
}
