package com.example.calculadoracorpo

import android.app.Application
import androidx.room.Room
import com.example.calculadoracorpo.data.local.AppDatabase
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.seunome.gorduracorporal.data.repository.CalculadoraGorduraCorporal

/**
 * Classe Application para inicializar e fornecer instâncias singleton.
 */
class CalculadoraCorpoApplication : Application() {

    // Instância do Banco de Dados
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "avaliacao_corporal_database"
        )
            .fallbackToDestructiveMigration() // Use com cautela em produção
            .build()
    }

    // Instância do Repositório (Corrigido listarTodosPacientes)
    val pacienteRepository: PacienteRepository by lazy {
        // Correção: A função listarTodosPacientes no repositório não deve pedir paciente.
        // Vamos assumir que foi corrigida para chamar pacienteDao.buscarTodos() diretamente.
        object : PacienteRepository(database.PacienteDao(), database.MedidasDao()) {
            override fun listarTodosPacientes(): kotlinx.coroutines.flow.Flow<List<com.example.calculadoracorpo.data.model.Paciente>> {
                return database.PacienteDao().buscarTodos()
            }
        }
        // Se você corrigiu o PacienteRepository original, pode usar:
        // PacienteRepository(database.PacienteDao(), database.MedidasDao())
    }

    // Instância da Calculadora
    val calculadoraGorduraCorporal: CalculadoraGorduraCorporal by lazy {
        CalculadoraGorduraCorporal()
    }
}