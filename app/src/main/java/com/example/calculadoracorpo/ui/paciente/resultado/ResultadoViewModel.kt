package com.example.calculadoracorpo.ui.resultado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.repository.OfflinePacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResultadoViewModel(
    private val repository: OfflinePacienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState

    fun processarNovaAvaliacao(
        pacienteId: Long,
        peso: Double,
        alturaCm: Int,
        idade: Int,
        medidas: Medidas
    ) {
        _uiState.value = ResultUiState.Loading

        viewModelScope.launch {
            try {
                val resultado = repository.realizarESalvarAvaliacao(
                    pacienteId = pacienteId,
                    peso = peso,
                    alturaCm = alturaCm,
                    idade = idade,
                    medidas = medidas
                )

                _uiState.value = ResultUiState.Success(resultado)

            } catch (e: Exception) {
                _uiState.value = ResultUiState.Error("Erro ao processar a avaliação: ${e.message}")
            }
        }
    }
}