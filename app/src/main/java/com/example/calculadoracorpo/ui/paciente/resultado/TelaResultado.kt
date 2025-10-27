package com.example.calculadoracorpo.ui.resultado

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para obter a ViewModel

// A ViewModel deve ser injetada, mas usamos 'viewModel()' aqui para simplificar a apresentação
// e obter a instância correta (já configurada no seu AppViewModelProvider).
@Composable
fun TelaResultado(
    viewModel: ResultadoViewModel = viewModel()
    // Geralmente, o NavController também é passado aqui se for necessário voltar para a lista.
) {
    // 1. Coleta e Observação do Estado
    // O 'collectAsState()' transforma o StateFlow da ViewModel em um State observável
    // pelo Compose. Sempre que o estado mudar, a UI será recompilada.
    val uiState by viewModel.uiState.collectAsState()

    // Ocupa a tela inteira com padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 2. Lógica de Exibição Baseada no Estado (UDF)
        when (uiState) {
            // --- Estado 1: Carregando (Loading) ---
            ResultUiState.Loading -> {
                // Exibe um indicador de progresso enquanto a Coroutine está rodando [cite: 1944, 2244]
                Text(text = "Calculando avaliação...", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            // --- Estado 2: Sucesso (Success) ---
            is ResultUiState.Success -> {
                // Desempacota o resultado para uso na função de exibição
                val resultado = (uiState as ResultUiState.Success).resultado

                // Exibe os detalhes formatados da avaliação
                ResultadoExibicao(resultado = resultado)
            }

            // --- Estado 3: Erro (Error) ---
            is ResultUiState.Error -> {
                // Exibe a mensagem de erro
                val mensagemErro = (uiState as ResultUiState.Error).message

                Text(
                    text = "Ocorreu um erro: $mensagemErro",
                    color = MaterialTheme.colorScheme.error, // Cor vermelha para erros
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Função auxiliar para organizar a exibição dos dados de ResultadoAvaliacao.
 */
@Composable
fun ResultadoExibicao(resultado: ResultadoAvaliacao) {
    Text(
        text = "Resultado da Avaliação (${resultado.dataAvaliacao})",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(32.dp))

    // Bloco IMC
    Text("IMC (Índice de Massa Corporal)", fontWeight = FontWeight.SemiBold)
    Text(
        text = "Valor: ${String.format("%.2f", resultado.imc)} - ${resultado.classificacaoImc}",
        fontSize = 20.sp
    )
    Spacer(Modifier.height(24.dp))

    // Bloco Composição Corporal
    Text("Composição Corporal", fontWeight = FontWeight.SemiBold)
    DadosLinha(label = "Protocolo:", valor = resultado.protocoloUtilizado)
    DadosLinha(label = "Densidade Corporal:", valor = String.format("%.4f g/ml", resultado.densidadeCorporal))
    DadosLinha(label = "Gordura Corporal:", valor = String.format("%.2f %%", resultado.percentualGordura))
    DadosLinha(label = "Massa Gorda:", valor = String.format("%.2f Kg", resultado.massaGordaKg))
    DadosLinha(label = "Massa Magra:", valor = String.format("%.2f Kg", resultado.massaMagraKg))

    // Adicione mais detalhes ou botões de ação conforme necessário (ex: Gerar PDF, Compartilhar)
}

/** Componente reusável para exibir um par de dados. */
@Composable
fun DadosLinha(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxSize()) {
        Text(text = label, fontWeight = FontWeight.Normal, fontSize = 16.sp)
        Text(text = valor, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
    }
}