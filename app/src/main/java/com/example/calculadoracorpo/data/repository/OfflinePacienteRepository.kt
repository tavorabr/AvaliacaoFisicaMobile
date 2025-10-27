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
 * (Seguindo a convenção 'Offline' vista nas aulas de Room [cite: 2288]).
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

    // O parâmetro 'paciente: Paciente' foi removido, pois não é usado em buscarTodos()
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

    // ATENÇÃO: Verifique se o tipo do ID no DAO é String ou Int para consistência.
    suspend fun buscarAvaliacaoAnterior(pacienteId: String, dataAtual: LocalDate): Medidas? {
        return medidasDao.buscarAvaliacaoAnterior(pacienteId, dataAtual)
    }

    // --- LÓGICA DE NEGÓCIO E PERSISTÊNCIA (Coroutines e Cálculo) ---

    /**
     * Realiza os cálculos de IMC/Composição Corporal, insere as medidas de entrada
     * no Room e retorna o objeto ResultadoAvaliacao.
     * * Usa Coroutines e Dispatchers.IO para a execução segura da lógica e E/S[cite: 2294, 2295].
     */
    suspend fun realizarESalvarAvaliacao(
        pacienteId: Long,
        peso: Double,
        alturaCm: Int,
        idade: Int,
        medidas: Medidas
    ): ResultadoAvaliacao {

        // Garante que o processamento pesado e o acesso ao banco de dados
        // ocorram em uma thread de I/O[cite: 2296, 2099].
        return withContext(Dispatchers.IO) {

            // 1. Cálculos de Risco (IMC)
            val imc = calcularIMC(peso, alturaCm)
            val classificacaoImc = classificarIMC(imc)

            // 2. Coleta de Medidas de Entrada
            val somaDobras = medidas.peitoral + medidas.axilarMedia + medidas.triceps +
                    medidas.subescapular + medidas.abdominal + medidas.suprailiaca +
                    medidas.coxa

            // 3. Cálculos Complexos (Densidade Corporal e % Gordura)
            val densidadeCorporal = calcularDensidadeCorporal(somaDobras, idade)
            val percentualGordura = calcularPercentualGordura(densidadeCorporal)

            // 4. Cálculos Finais de Massa
            val massaGordaKg = (peso * percentualGordura) / 100.0
            val massaMagraKg = peso - massaGordaKg

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
        // Usa a estrutura de desvio 'when' [cite: 506]
        return when {
            imc < 18.5 -> "Baixo Peso"
            imc < 25.0 -> "Peso Normal"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obesidade"
        }
    }

    private fun calcularDensidadeCorporal(somaDobras: Double, idade: Int): Double {
        // FÓRMULA EXEMPLO para demonstração da lógica de cálculo.
        return 1.112 - (0.00043499 * somaDobras) + (0.00000055 * (somaDobras * somaDobras)) - (0.00028826 * idade)
    }

    private fun calcularPercentualGordura(densidadeCorporal: Double): Double {
        // Fórmula de Siri: %G = (495 / D) - 450
        return (495.0 / densidadeCorporal) - 450.0
    }
}