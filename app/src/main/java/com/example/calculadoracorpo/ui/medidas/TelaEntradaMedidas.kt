package com.example.calculadoracorpo.ui.medidas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField // Elemento de entrada de dados
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // State Mutável [cite: 1791]
import androidx.compose.runtime.remember // Gerenciamento de Estado Local [cite: 1791]
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.data.model.Medidas // Entity para salvar
import com.example.calculadoracorpo.data.model.Paciente // Assumindo que Paciente está disponível
import com.example.calculadoracorpo.data.model.Protocolo // Assumindo Enum Protocolo
import com.example.calculadoracorpo.ui.resultado.ResultadoViewModel
import com.example.calculadoracorpo.ui.paciente.detail.PacienteDetailViewModel // Assumindo que o detalhe do paciente pode ser buscado aqui
import java.time.LocalDate

// Constantes para simular dados do Paciente
private const val IDADE_PLACEHOLDER = 23 // Deve ser obtido do Paciente.kt

@Composable
fun TelaEntradaMedidas(
    pacienteId: Int,
    resultadoViewModel: ResultadoViewModel = viewModel(factory = AppViewModelProvider.Factory),
    medidasEntryViewModel: MedidasEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSaveAndNavigate: () -> Unit,
    onCancel: () -> Unit
) {
    // Simulação do Paciente - Em um App real, você buscará o Paciente pelo ID
    // Usaremos a ViewModel de Detalhes para simular a busca.
    val pacienteDetailViewModel: PacienteDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
    // pacienteDetailViewModel.uiState.collectAsState() e pegar o nome/idade/peso

    // Estado da UI para os campos de entrada (State Hoisting simplificado)
    var altura by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var peitoral by remember { mutableStateOf("") }
    var abdominal by remember { mutableStateOf("") }
    var triceps by remember { mutableStateOf("") }
    var axilarMedia by remember { mutableStateOf("") }
    var subescapular by remember { mutableStateOf("") }
    var supraIliaca by remember { mutableStateOf("") }
    var coxa by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nova Avaliação - Paciente ID: $pacienteId",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))

        // --- Dados Básicos (Peso e Altura) ---
        InputCampo(label = "Altura (cm)", valor = altura, onValueChange = { altura = it })
        InputCampo(label = "Peso (Kg)", valor = peso, onValueChange = { peso = it })

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Dobras Cutâneas (mm)",
            style = MaterialTheme.typography.headlineSmall
        )

        // --- Dobras Cutâneas (7 Dobras) ---
        // Se for um protocolo de 3 dobras, você esconderia ou desabilitaria o resto.
        InputCampo(label = "Peitoral", valor = peitoral, onValueChange = { peitoral = it })
        InputCampo(label = "Axilar Média", valor = axilarMedia, onValueChange = { axilarMedia = it })
        InputCampo(label = "Tríceps", valor = triceps, onValueChange = { triceps = it })
        InputCampo(label = "Subescapular", valor = subescapular, onValueChange = { subescapular = it })
        InputCampo(label = "Abdominal", valor = abdominal, onValueChange = { abdominal = it })
        InputCampo(label = "Suprailíaca", valor = supraIliaca, onValueChange = { supraIliaca = it })
        InputCampo(label = "Coxa", valor = coxa, onValueChange = { coxa = it })

        Spacer(Modifier.height(24.dp))

        // --- Botão de Ação ---
        Button(
            onClick = {
                // 1. Coleta e Valida os dados de entrada
                val medidasParaSalvar = Medidas(
                    id = 0,
                    pacienteId = pacienteId,
                    dataAvaliacao = LocalDate.now(),
                    altura = altura.toDoubleOrNull(),
                    peso = peso.toDoubleOrNull(),
                    protocoloUsado = Protocolo.SETE_DOBRAS, // Mock de Protocolo
                    peitoral = peitoral.toDoubleOrNull(),
                    abdominal = abdominal.toDoubleOrNull(),
                    triceps = triceps.toDoubleOrNull(),
                    axilarMedia = axilarMedia.toDoubleOrNull(),
                    subescapular = subescapular.toDoubleOrNull(),
                    supraIliaca = supraIliaca.toDoubleOrNull(),
                    coxa = coxa.toDoubleOrNull()
                )

                // 2. Dispara o processamento assíncrono na ViewModel de Resultados
                resultadoViewModel.processarNovaAvaliacao(
                    pacienteId = pacienteId.toLong(),
                    peso = medidasParaSalvar.peso ?: 0.0, // Passa valores não-nulos
                    alturaCm = (medidasParaSalvar.altura ?: 0.0).toInt(),
                    idade = IDADE_PLACEHOLDER,
                    medidas = medidasParaSalvar
                )

                // 3. Navega imediatamente para a tela de resultado (que estará em Loading)
                onSaveAndNavigate()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ADICIONAR MEDIDAS E VER RESULTADO")
        }

        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
        ) {
            Text("CANCELAR")
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** Componente customizado para entrada de dados numéricos (Dobras). */
@Composable
fun InputCampo(
    label: String,
    valor: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberDecimal),
        modifier = Modifier.fillMaxWidth()
    )
}

// MOCK de Protocolo para fins de compilação
enum class Protocolo {
    TRES_DOBRAS, SETE_DOBRAS
}