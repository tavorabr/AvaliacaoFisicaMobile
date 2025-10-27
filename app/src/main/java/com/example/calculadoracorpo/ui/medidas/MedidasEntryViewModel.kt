package com.example.calculadoracorpo.ui.medidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Protocolo // Assumindo que Protocolo existe
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.repository.OfflinePacienteRepository
import com.example.calculadoracorpo.ui.resultado.ResultadoViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel para gerenciar a entrada de dados (dobras cutâneas) na Tela de Medidas.
 */
class MedidasEntryViewModel(
    private val pacienteRepository: OfflinePacienteRepository
) : ViewModel() {

    // Estado do paciente atual e medidas temporárias (para campos de texto)
    // O ideal é usar MutableStateFlow ou State do Compose
    // Usaremos um simples data class para o estado da UI de entrada.
    private val _medidasUiState = MutableStateFlow(MedidasEntryUiState())
    val medidasUiState: StateFlow<MedidasEntryUiState> = _medidasUiState

    /**
     * Atualiza o estado das medidas temporárias, garantindo o 'State Hoisting'.
     */
    fun updateMedidasState(medidasDetalhadas: MedidasDetalhadas) {
        _medidasUiState.value = MedidasEntryUiState(medidasDetalhadas)
    }
}

/** * Classe interna para o estado da UI (campos de texto)
 * Você usaria o remember { mutableStateOf(...) } na Composable ou aqui.
 */
data class MedidasEntryUiState(
    val medidasDetalhadas: MedidasDetalhadas = MedidasDetalhadas()
)

data class MedidasDetalhadas(
    val altura: String = "",
    val peso: String = "",
    val protocolo: Protocolo = Protocolo.SETE_DOBRAS,
    // Usando String para simular o valor digitado no TextField
    val peitoral: String = "",
    val abdominal: String = "",
    val triceps: String = "",
    val axilarMedia: String = "",
    val subescapular: String = "",
    val supraIliaca: String = "",
    val coxa: String = ""
)


/**
 * Função que simula o botão "ADICIONAR MEDIDAS" e chama o ResultadoViewModel.
 * OBS: Esta função precisa ser colocada na ViewModel que lida com a entrada de dados,
 * ou passada como um callback na tela.
 * O método mais limpo é o ResultadoViewModel ser chamado diretamente da tela, após o clique,
 * por uma função que coordena a ação e a navegação.
 * Vamos criar uma função de alto nível para isso, simulando o que aconteceria após o clique:
 */
fun onAdicionarMedidasClicked(
    medidasDetalhes: MedidasDetalhadas,
    pacienteAtual: Paciente, // Paciente carregado na tela de detalhes
    resultadoViewModel: ResultadoViewModel,
    navigateToResultado: () -> Unit // Função para navegar para a TelaResultado
) {
    // 1. Converter Strings de entrada para Double (Tratamento de erro necessário)
    val altura = medidasDetalhes.altura.toDoubleOrNull() ?: 0.0
    val peso = medidasDetalhes.peso.toDoubleOrNull() ?: 0.0

    // 2. Criar o objeto Medidas (Entidade Room) com os dados não-nulos
    val medidasParaSalvar = Medidas(
        id = 0, // Id será gerado automaticamente (se configurado com autoGenerate = true)
        pacienteId = pacienteAtual.id, // Assumindo que o Paciente tem um campo 'id: Int'
        dataAvaliacao = LocalDate.now(),
        altura = altura.toDouble(),
        peso = peso.toDouble(),
        protocoloUsado = medidasDetalhes.protocolo,
        peitoral = medidasDetalhes.peitoral.toDoubleOrNull(),
        abdominal = medidasDetalhes.abdominal.toDoubleOrNull(),
        triceps = medidasDetalhes.triceps.toDoubleOrNull(),
        axilarMedia = medidasDetalhes.axilarMedia.toDoubleOrNull(),
        subescapular = medidasDetalhes.subescapular.toDoubleOrNull(),
        supraIliaca = medidasDetalhes.supraIliaca.toDoubleOrNull(),
        coxa = medidasDetalhes.coxa.toDoubleOrNull()
    )

    // 3. Chamar o processamento no ResultadoViewModel
    resultadoViewModel.processarNovaAvaliacao(
        pacienteId = pacienteAtual.id.toLong(),
        peso = peso,
        alturaCm = altura.toInt(),
        idade = 23, // O ideal é calcular/obter a idade do objeto Paciente
        medidas = medidasParaSalvar
    )

    // 4. Navegar para a tela de resultado (Passo de Navegação)
    navigateToResultado()
}