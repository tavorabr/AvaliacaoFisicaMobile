package com.example.calculadoracorpo.ui.paciente.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.repository.PacienteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PacienteListViewModel(
    private val pacienteRepository: PacienteRepository
) : ViewModel() {

    // Usa StateFlow para o estado da UI
    private val _uiState = MutableStateFlow(PacienteListUiState(isLoading = true))
    val uiState: StateFlow<PacienteListUiState> = _uiState.asStateFlow()

    init {
        collectPacientes()
    }

    private fun collectPacientes() {
        viewModelScope.launch {
            // Assumindo que listarTodosPacientes no repo foi corrigido
            pacienteRepository.listarTodosPacientes()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao buscar pacientes: ${exception.message}"
                        )
                    }
                }
                .collect { listaPacientes ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pacientes = listaPacientes,
                            error = null // Limpa erro em caso de sucesso
                        )
                    }
                }
        }
    }

    // Função para deletar paciente (exemplo)
    fun deletePaciente(paciente: com.example.calculadoracorpo.data.model.Paciente) {
        viewModelScope.launch {
            try {
                pacienteRepository.excluirPaciente(paciente)
                // A lista será atualizada automaticamente pelo Flow
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erro ao excluir paciente: ${e.message}")
                }
            }
        }
    }
}
