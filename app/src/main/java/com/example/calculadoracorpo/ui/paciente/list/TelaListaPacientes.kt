package com.example.calculadoracorpo.ui.paciente.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Ícone de deletar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.di.AppViewModelProvider
import com.example.calculadoracorpo.ui.theme.CalculadoracorpoTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaPacientes(
    onPacienteClick: (Int) -> Unit,
    onAddPacienteClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PacienteListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Para lançar a exclusão

    // Mostra Snackbar em caso de erro
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Erro: $it")
            // TODO: Adicionar lógica para limpar o erro no ViewModel após mostrar
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Pacientes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPacienteClick) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Paciente")
            }
        },
        modifier = modifier
    ) { innerPadding ->

        when {
            // Estado de Carregamento
            uiState.isLoading -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Lista Vazia
            uiState.pacientes.isEmpty() && !uiState.isLoading -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum paciente cadastrado.", textAlign = TextAlign.Center)
                }
            }
            // Lista com Dados
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.pacientes, key = { it.id }) { paciente ->
                        ItemPacienteList(
                            paciente = paciente,
                            onClick = { onPacienteClick(paciente.id) },
                            onDeleteClick = { viewModel.deletePaciente(paciente) } // Chama delete no ViewModel
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemPacienteList(
    paciente: Paciente,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit, // Callback para deletar
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Adicionar imagem placeholder ou foto do paciente se tiver
            // Image(...)
            // Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paciente.nome,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${paciente.idade} anos - ${paciente.sexo.name.lowercase().replaceFirstChar { it.titlecase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Botão de deletar
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir Paciente",
                    tint = MaterialTheme.colorScheme.error // Cor vermelha para indicar perigo
                )
            }
        }
    }
}


// --- Previews ---
@Preview(showBackground = true)
@Composable
fun PreviewTelaListaPacientesComDados() {
    CalculadoracorpoTheme {
        val pacientesPreview = listOf(
            Paciente(1, "João Silva", LocalDate.of(1990, 5, 15), Sexo.MASCULINO),
            Paciente(2, "Maria Oliveira", LocalDate.of(1985, 10, 20), Sexo.FEMININO)
        )
        // Simula o ViewModel com dados (necessário mock mais robusto para ações)
        val mockUiState = PacienteListUiState(pacientes = pacientesPreview)
        // Como o ViewModel agora tem lógica, o preview direto fica complexo.
        // O ideal é testar em emulador/dispositivo ou usar frameworks de preview avançados.
        // Exibindo apenas a lista estática para visualização:
        Scaffold(topBar = { TopAppBar(title = { Text("Pacientes (Preview)") }) }) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pacientesPreview) { p -> ItemPacienteList(p, {}, {}) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaListaPacientesVazia() {
    CalculadoracorpoTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Pacientes (Preview Vazio)") }) }) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum paciente cadastrado.")
            }
        }
    }
}
