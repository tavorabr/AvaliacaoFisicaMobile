package com.example.calculadoracorpo.ui.paciente.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.ui.navigation.Routes
import com.seunome.gorduracorporal.data.repository.CalculadoraGorduraCorporal
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado da UI para os detalhes do paciente
data class PacienteDetailUiState(
    val paciente: Paciente? = null,
    val avaliacoes: List<MedidaItemUiState> = emptyList(), // Lista de avaliações formatadas
    val isLoading: Boolean = true,
    val error: String? = null
)

// Representa uma avaliação na lista, com cálculo de gordura
data class MedidaItemUiState(
    val medida: Medidas,
    val gorduraCalculada: Double? = null // Percentual de gordura calculado
)

class PacienteDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val pacienteRepository: PacienteRepository,
    private val calculadoraGordura: CalculadoraGorduraCorporal // Adicionado
) : ViewModel() {

    private val pacienteId: Int = checkNotNull(savedStateHandle[Routes.ARG_PACIENTE_ID])

    private val _uiState = MutableStateFlow(PacienteDetailUiState())
    val uiState: StateFlow<PacienteDetailUiState> = _uiState.asStateFlow()

    init {
        loadPacienteData()
    }

    private fun loadPacienteData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Carrega dados do paciente em paralelo com as avaliações
                val pacienteFlow = flow { emit(pacienteRepository.buscarPaciente(pacienteId)) }
                val avaliacoesFlow = pacienteRepository.buscarAvaliacoes(pacienteId) // Já é um Flow

                // Combina os resultados quando ambos estiverem prontos
                combine(pacienteFlow, avaliacoesFlow) { paciente, listaMedidas ->
                    if (paciente == null) {
                        throw IllegalStateException("Paciente não encontrado")
                    }
                    // Calcula a gordura para cada medida antes de atualizar o estado
                    val avaliacoesComGordura = listaMedidas.map { medida ->
                        val gordura = calculadoraGordura.calcularGordura(medida, paciente)
                        MedidaItemUiState(medida = medida, gorduraCalculada = gordura)
                    }

                    PacienteDetailUiState(
                        paciente = paciente,
                        avaliacoes = avaliacoesComGordura,
                        isLoading = false
                    )
                }
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = "Erro ao carregar dados: ${e.message}") } }
                    .collect { newState -> _uiState.value = newState }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erro: ${e.message}") }
            }
        }
    }

    // Função para excluir medida (exemplo)
    fun deleteMedida(medida: Medidas) {
        viewModelScope.launch {
            try {
                // O DAO de Medidas precisa ter a função excluir
                // pacienteRepository.excluirAvaliacao(medida) // Supondo que exista no repo/dao
                println("Excluir medida ${medida.id} - Funcionalidade a implementar no DAO/Repo")
                _uiState.update { it.copy(error = "Exclusão de medida não implementada no DAO/Repo.")}

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao excluir medida: ${e.message}")}
            }
        }
    }
}
