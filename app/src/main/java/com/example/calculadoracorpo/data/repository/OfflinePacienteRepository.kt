package com.example.calculadoracorpo.data.repository

import com.example.calculadoracorpo.data.local.MedidasDao
import com.example.calculadoracorpo.data.local.PacienteDao
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.ResultadoAvaliacao // Requer a criação desta data class
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** * Implementação concreta do Repositório que lida com as fontes de dados locais (Room)
 * e executa a lógica de cálculo da avaliação física.
 */
class OfflinePacienteRepository(
    private val pacienteDao: PacienteDao,
    private val medidasDao: MedidasDao
) {
    // --- CRUD Do Paciente ---
    suspend fun inserirPaciente(paciente: Paciente) = pacienteDao.inserir(paciente)
    suspend fun editarPaciente(paciente: Paciente) = pacienteDao.atualizar(paciente)
    suspend fun excluirPaciente(paciente: Paciente) = pacienteDao.deletar(paciente)
    suspend fun buscarPaciente(id: Int): Paciente? = pacienteDao.buscarPorId(id)

    fun listarTodosPacientes(): Flow<List<Paciente>> = pacienteDao.buscarTodos()

    // --- CRUD Das Medidas / Avaliações ---
    suspend fun inserirAvaliacao(medidas: Medidas) = medidasDao.inserir(medidas)
    fun buscarAvaliacoes(pacienteId: Int): Flow<List<Medidas>> {
        return medidasDao.buscarAvaliacoesdoPaciente(pacienteId)
    }
    suspend fun buscarPrimeiraAvaliacao(pacienteId: Int): Medidas? {
        return medidasDao.buscarPrimeiraAvaliacao(pacienteId)
    }
    suspend fun buscarUltimaAvaliacao(pacienteId: Int): Medidas? {
        return medidasDao.buscarUltimaAvaliacao(pacienteId)
    }
    suspend fun buscarAvaliacaoAnterior(pacienteId: String, dataAtual: LocalDate): Medidas? {
        return medidasDao.buscarAvaliacaoAnterior(pacienteId, dataAtual)
    }

    // --- LÓGICA DE NEGÓCIO E PERSISTÊNCIA (Coroutines e Cálculo) ---

    /**
     * Realiza os cálculos de IMC/Composição Corporal, insere as medidas de entrada
     * no Room e retorna o objeto ResultadoAvaliacao.
     */
    suspend fun realizarESalvarAvaliacao(
        pacienteId: Long,
        // REMOVIDOS peso e alturaCm daqui, pois serão obtidos da Medidas
        idade: Int,
        medidas: Medidas // Contém peso, altura, e dobras
    ): ResultadoAvaliacao {

        return withContext(Dispatchers.IO) {

            // Extrai peso e altura da Medidas, usando 0.0 caso sejam nulos ou 0
            val pesoKg = medidas.peso ?: 0.0
            val alturaCm = medidas.altura ?: 0.0

            if (pesoKg <= 0.0 || alturaCm <= 0.0) {
                throw IllegalArgumentException("Peso e altura devem ser valores válidos para o cálculo.")
            }

            // 1. Cálculos de Risco (IMC)
            val imc = calcularIMC(pesoKg, alturaCm.toInt())
            val classificacaoImc = classificarIMC(imc)

            // 2. Coleta de Medidas de Entrada (USANDO PROPRIEDADE NÂO-NULA)
            val somaDobras = medidas.somaDobrasNaoNula

            // 3. Cálculos Complexos (Densidade Corporal e % Gordura)
            val densidadeCorporal = calcularDensidadeCorporal(somaDobras, idade)
            val percentualGordura = calcularPercentualGordura(densidadeCorporal)

            // 4. Cálculos Finais de Massa
            val massaGordaKg = (pesoKg * percentualGordura) / 100.0
            val massaMagraKg = pesoKg - massaGordaKg

            // 5. Persistência da Medida de Entrada (Room)
            medidasDao.inserir(medidas)

            // 6. Retorno do Objeto de Resultado (Data Class)
            ResultadoAvaliacao(
                avaliacaoId = System.currentTimeMillis(),
                pacienteId = pacienteId,
                dataAvaliacao = LocalDate.now().toString(),
                imc = imc,
                classificacaoImc = classificacaoImc,
                densidadeCorporal = densidadeCorporal,
                percentualGordura = percentualGordura,
                massaGordaKg = massaGordaKg,
                massaMagraKg = massaMagraKg,
                protocoloUtilizado = "7 Dobras (Jackson/Pollock)"
            )
        }
    }

    // --- FUNÇÕES PRIVADAS DE CÁLCULO ---

    private fun calcularIMC(pesoKg: Double, alturaCm: Int): Double {
        val alturaM = alturaCm / 100.0
        return pesoKg / (alturaM * alturaM)
    }

    private fun classificarIMC(imc: Double): String {
        return when {
            imc < 18.5 -> "Baixo Peso"
            imc < 25.0 -> "Peso Normal"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obesidade"
        }
    }

    private fun calcularDensidadeCorporal(somaDobras: Double, idade: Int): Double {
        return 1.112 - (0.00043499 * somaDobras) + (0.00000055 * (somaDobras * somaDobras)) - (0.00028826 * idade)
    }

    private fun calcularPercentualGordura(densidadeCorporal: Double): Double {
        return (495.0 / densidadeCorporal) - 450.0
    }
}