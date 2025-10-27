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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.data.model.ResultadoAvaliacao

@Composable
fun TelaResultado(
    viewModel: ResultadoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            ResultUiState.Loading -> {
                Text(text = "Calculando avaliação...", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
            is ResultUiState.Success -> {
                val resultado = (uiState as ResultUiState.Success).resultado
                ResultadoExibicao(resultado = resultado)
            }
            is ResultUiState.Error -> {
                val mensagemErro = (uiState as ResultUiState.Error).message

                Text(
                    text = "Ocorreu um erro: $mensagemErro",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ResultadoExibicao(resultado: ResultadoAvaliacao) {
    Text(
        text = "Resultado da Avaliação (${resultado.dataAvaliacao})",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(32.dp))

    Text("IMC (Índice de Massa Corporal)", fontWeight = FontWeight.SemiBold)
    DadosLinha(label = "Valor:", valor = String.format("%.2f - %s", resultado.imc, resultado.classificacaoImc))
    Spacer(Modifier.height(24.dp))

    Text("Composição Corporal", fontWeight = FontWeight.SemiBold)
    DadosLinha(label = "Protocolo:", valor = resultado.protocoloUtilizado)
    DadosLinha(label = "Densidade Corporal:", valor = String.format("%.4f g/ml", resultado.densidadeCorporal))
    DadosLinha(label = "Gordura Corporal:", valor = String.format("%.2f %%", resultado.percentualGordura))
    DadosLinha(label = "Massa Gorda:", valor = String.format("%.2f Kg", resultado.massaGordaKg))
    DadosLinha(label = "Massa Magra:", valor = String.format("%.2f Kg", resultado.massaMagraKg))
}

@Composable
fun DadosLinha(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxSize()) {
        Text(text = label, fontWeight = FontWeight.Normal, fontSize = 16.sp)
        Text(text = valor, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
    }
}