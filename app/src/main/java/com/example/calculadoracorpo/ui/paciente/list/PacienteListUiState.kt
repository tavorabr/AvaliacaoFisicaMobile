package com.example.calculadoracorpo.ui.paciente.list

import com.example.calculadoracorpo.data.model.Paciente

/**
 * Representa o estado da UI para a TelaListaPacientes.
 */
data class PacienteListUiState(
    val pacientes: List<Paciente> = emptyList(),
    val isLoading: Boolean = false, // Para indicar carregamento inicial
    val error: String? = null      // Para mensagens de erro
)
