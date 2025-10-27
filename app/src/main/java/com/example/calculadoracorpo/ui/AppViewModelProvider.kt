package com.example.calculadoracorpo.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calculadoracorpo.CalculadoraCorpoApplication
import com.example.calculadoracorpo.data.repository.OfflinePacienteRepository
import com.example.calculadoracorpo.ui.medidas.MedidasEntryViewModel
import com.example.calculadorcorpo.ui.paciente.detail.PacienteDetailViewModel
import com.example.calculadorcorpo.ui.paciente.edit.PacienteEditViewModel
import com.example.calculadoracorpo.ui.paciente.list.PacienteListViewModel
import com.example.calculadorocorpo.ui.resultado.ResultadoViewModel

/**
 * Fornecedor de Factory para a criação de ViewModels.
 * Garante que cada ViewModel receba as dependências corretas (como o Repositório).
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {

        // --- 1. PacienteListViewModel ---
        initializer {
            PacienteListViewModel(
                calculadoraCorpoApplication().container.pacienteRepository as OfflinePacienteRepository
            )
        }

        // --- 2. PacienteDetailViewModel ---
        initializer {
            PacienteDetailViewModel(
                this.createSavedStateHandle(),
                calculadoraCorpoApplication().container.pacienteRepository as OfflinePacienteRepository
            )
        }

        // --- 3. PacienteEditViewModel ---
        initializer {
            PacienteEditViewModel(
                this.createSavedStateHandle(),
                calculadoraCorpoApplication().container.pacienteRepository as OfflinePacienteRepository
            )
        }

        // --- 4. ResultadoViewModel ---
        initializer {
            ResultadoViewModel(
                repository = calculadoraCorpoApplication().container.pacienteRepository as OfflinePacienteRepository
            )
        }

        // --- 5. MedidasEntryViewModel ---
        initializer {
            MedidasEntryViewModel(
                pacienteRepository = calculadoraCorpoApplication().container.pacienteRepository as OfflinePacienteRepository
            )
        }
    }
}

/**
 * Função de extensão que resolve a referência à classe Application.
 */
fun CreationExtras.calculadoraCorpoApplication(): CalculadoraCorpoApplication =
    (this[APPLICATION_KEY] as CalculadoraCorpoApplication)