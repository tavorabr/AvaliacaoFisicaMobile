// Em: com.example.calculadoracorpo.ui.resultado.ResultadoViewModel.kt

package com.example.calculadoracorpo.ui.resultado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Usado para Coroutines com ciclo de vida do ViewModel [cite: 2247]
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.repository.OfflinePacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch // Usado para iniciar a coroutine [cite: 2160, 2215]

/**
 * Gerencia o estado da Tela de Resultados, chamando o repositório para realizar os cálculos.
 */
class ResultadoViewModel(
    private val repository: OfflinePacienteRepository
) : ViewModel() {

    // MutableStateFlow para o estado ser mutável internamente
    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)

    // StateFlow para expor o estado imutável para a UI (princípio do UDF [cite: 1917])
    val uiState: StateFlow<ResultUiState> = _uiState

    /**
     * Inicia o processo de cálculo e persistência da avaliação física.
     * Esta função será chamada a partir da Composable de Medidas após a entrada de dados.
     * * @param pacienteId O ID do paciente sendo avaliado.
     * @param peso O peso atual do paciente em Kg.
     * @param alturaCm A altura atual do paciente em cm.
     * @param idade A idade (crucial para as fórmulas de densidade).
     * @param medidas O objeto com as dobras cutâneas.
     */
    fun processarNovaAvaliacao(
        pacienteId: Long,
        peso: Double,
        alturaCm: Int,
        idade: Int,
        medidas: Medidas
    ) {
        // Define o estado inicial como carregando
        _uiState.value = ResultUiState.Loading

        // Inicia a coroutine no escopo do ViewModel, garantindo que a operação
        // será cancelada se o ViewModel for destruído[cite: 2247, 2196].
        viewModelScope.launch {
            try {
                // Chama a função suspend do repositório
                val resultado = repository.realizarESalvarAvaliacao(
                    pacienteId = pacienteId,
                    peso = peso,
                    alturaCm = alturaCm,
                    idade = idade,
                    medidas = medidas
                )

                // Atualiza o StateFlow com sucesso
                _uiState.value = ResultUiState.Success(resultado)

            } catch (e: Exception) {
                // Captura e expõe qualquer erro que venha do cálculo ou do Room
                _uiState.value = ResultUiState.Error("Erro ao processar a avaliação: ${e.message}")
            }
        }
    }
}