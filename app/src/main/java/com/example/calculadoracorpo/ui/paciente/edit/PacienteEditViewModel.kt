package com.example.calculadoracorpo.ui.paciente.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.ui.navigation.Routes
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Estado da UI para a tela de edição
data class PacienteEditUiState(
    val id: Int? = null, // Nulo se for adição, preenchido se for edição
    val nome: String = "",
    val dataNascimentoString: String = "", // Formato "dd/MM/yyyy"
    val sexo: Sexo? = null,
    val isLoading: Boolean = false,
    val isPacienteLoaded: Boolean = false, // Para saber se carregou dados para edição
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false // Flag para indicar sucesso e navegar de volta
) {
    // Validação simples
    val isFormValid: Boolean
        get() = nome.isNotBlank() && dataNascimentoString.isNotBlank() && sexo != null && getDataNascimento() != null

    // Função auxiliar para converter String em LocalDate
    fun getDataNascimento(): LocalDate? {
        return try {
            LocalDate.parse(dataNascimentoString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: DateTimeParseException) {
            null
        }
    }
}


class PacienteEditViewModel(
    savedStateHandle: SavedStateHandle, // Para obter o ID da navegação
    private val pacienteRepository: PacienteRepository
) : ViewModel() {

    var uiState by mutableStateOf(PacienteEditUiState())
        private set

    // Obtém o ID do paciente dos argumentos de navegação (se houver)
    private val pacienteId: Int? = savedStateHandle[Routes.ARG_PACIENTE_ID] // Pode ser nulo

    init {
        if (pacienteId != null && pacienteId != -1) { // Verifica se é modo edição
            loadPaciente(pacienteId)
        } else {
            // Modo adição, estado inicial já está ok
            uiState = uiState.copy(isPacienteLoaded = true) // Marca como "carregado" (não precisou carregar)
        }
    }

    private fun loadPaciente(id: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val paciente = pacienteRepository.buscarPaciente(id)
                if (paciente != null) {
                    uiState = uiState.copy(
                        id = paciente.id,
                        nome = paciente.nome,
                        dataNascimentoString = paciente.dataDeNascimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        sexo = paciente.sexo,
                        isLoading = false,
                        isPacienteLoaded = true
                    )
                } else {
                    // Paciente não encontrado
                    uiState = uiState.copy(isLoading = false, isPacienteLoaded = true, errorMessage = "Paciente não encontrado.")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, isPacienteLoaded = true, errorMessage = "Erro ao carregar paciente: ${e.message}")
            }
        }
    }

    // --- Funções para atualizar campos ---
    fun onNomeChange(value: String) {
        uiState = uiState.copy(nome = value, errorMessage = null)
    }

    fun onDataNascimentoChange(value: String) {
        // Validação simples de formato enquanto digita (permite números e barras)
        if (value.all { it.isDigit() || it == '/' } && value.length <= 10) {
            uiState = uiState.copy(dataNascimentoString = value, errorMessage = null)
        }
    }

    fun onSexoChange(value: Sexo) {
        uiState = uiState.copy(sexo = value, errorMessage = null)
    }

    // --- Salvar Paciente ---
    fun savePaciente() {
        val dataNascimento = uiState.getDataNascimento()

        // Validar antes de salvar
        if (!uiState.isFormValid) {
            uiState = uiState.copy(errorMessage = "Preencha todos os campos corretamente.")
            return
        }
        if (dataNascimento == null) {
            uiState = uiState.copy(errorMessage = "Data de nascimento inválida (use dd/MM/yyyy).")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val paciente = Paciente(
                    // Gera um ID novo se for adição (ex: timestamp ou UUID - Room pode autogerar se configurado)
                    // Para Room Autogenerate: id = if (uiState.id != null) uiState.id!! else 0
                    id = uiState.id ?: System.currentTimeMillis().toInt(), // ID simples para exemplo
                    nome = uiState.nome,
                    dataDeNascimento = dataNascimento,
                    sexo = uiState.sexo!! // Já validado que não é nulo
                )

                if (uiState.id != null) {
                    // Modo Edição
                    pacienteRepository.editarPaciente(paciente)
                } else {
                    // Modo Adição
                    pacienteRepository.inserirPaciente(paciente)
                }
                uiState = uiState.copy(isLoading = false, saveSuccess = true) // Sinaliza sucesso

            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Erro ao salvar: ${e.message}")
            }
        }
    }

    // Função para resetar o flag de sucesso (chamado após navegar de volta)
    fun resetSaveSuccess() {
        uiState = uiState.copy(saveSuccess = false)
    }
}
