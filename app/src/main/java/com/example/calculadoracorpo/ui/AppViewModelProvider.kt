package com.example.calculadoracorpo.di // Coloque no pacote di ou ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calculadoracorpo.CalculadoraCorpoApplication
import com.example.calculadoracorpo.ui.screens.MainScreenViewModel // Corrija o import se mover o ViewModel

/**
 * Factory Provider para todos os ViewModels da aplicação.
 */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        // --- Inicializador para MainScreenViewModel ---
        initializer {
            val application = calculadoraCorpoApplication()
            // Obtém as instâncias da Application
            val repository = application.pacienteRepository
            val calculadora = application.calculadoraGorduraCorporal
            // Cria o ViewModel passando as dependências
            MainScreenViewModel(
                pacienteRepository = repository, // Usaremos para salvar/carregar no futuro
                calculadoraGordura = calculadora
            )
        }

        // --- Adicione inicializadores para outros ViewModels aqui ---
    }
}

/**
 * Extension function para acessar facilmente a instância da Application
 * a partir das CreationExtras dentro dos inicializadores da Factory.
 */
fun CreationExtras.calculadoraCorpoApplication(): CalculadoraCorpoApplication {
    return (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CalculadoraCorpoApplication)
}
