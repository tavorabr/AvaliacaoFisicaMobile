package com.example.calculadoracorpo.ui.resultado

import com.example.calculadoracorpo.data.model.ResultadoAvaliacao

sealed interface ResultUiState {
    object Loading : ResultUiState
    data class Success(val resultado: ResultadoAvaliacao) : ResultUiState
    data class Error(val message: String) : ResultUiState
}