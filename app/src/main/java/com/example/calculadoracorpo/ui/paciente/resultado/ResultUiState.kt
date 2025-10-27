// Em: com.example.calculadoracorpo.ui.resultado.ResultUiState.kt

import com.example.calculadoracorpo.data.model.ResultadoAvaliacao

/**
 * Representa o estado da UI para a Tela de Resultados.
 * Usa um sealed interface/class para representar os diferentes estados de carregamento.
 * Isto segue o princípio do UDF (Unidirectional Data Flow)[cite: 1907, 1913].
 */
sealed interface ResultUiState {

    /** Estado de carregamento inicial, o cálculo está sendo realizado (Coroutines em execução). */
    object Loading : ResultUiState

    /** Estado onde o resultado da avaliação foi carregado com sucesso. */
    data class Success(val resultado: ResultadoAvaliacao) : ResultUiState

    /** Estado de erro, se o cálculo falhar (ex: divisão por zero ou erro do BD). */
    data class Error(val message: String) : ResultUiState
}