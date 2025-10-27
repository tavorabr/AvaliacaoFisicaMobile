package com.example.calculadoracorpo.ui.paciente.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.di.AppViewModelProvider
import com.example.calculadoracorpo.ui.theme.CalculadoracorpoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacienteDetailScreen(
    onEditClick: () -> Unit,
    onAddMedidaClick: () -> Unit,
    onMedidaClick: (Int) -> Unit, // Passa o ID da medida clicada
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PacienteDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efeito para mostrar Snackbar em caso de erro
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Erro: $it")
            // TODO: Adicionar lógica para limpar o erro no ViewModel após mostrar
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.paciente?.nome ?: "Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Botão Editar Paciente
                    IconButton(onClick = onEditClick, enabled = uiState.paciente != null) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar Paciente")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedidaClick) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Avaliação")
            }
        },
        modifier = modifier
    ) { innerPadding ->

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.paciente == null -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Paciente não encontrado.")
                }
            }
            else -> {
                // Usa LazyColumn para o caso de muitas avaliações
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Seção de Informações do Paciente
                    item {
                        PacienteInfoCard(paciente = uiState.paciente!!)
                    }

                    // Seção Cabeçalho da Lista de Avaliações
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Histórico de Avaliações", style = MaterialTheme.typography.titleMedium)
                            // TODO: Adicionar botão/ícone para ordenar ou filtrar se necessário
                        }
                        Divider()
                    }

                    // Lista de Avaliações
                    if (uiState.avaliacoes.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhuma avaliação registrada.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.avaliacoes, key = { it.medida.id }) { avaliacaoItem ->
                            AvaliacaoItemCard(
                                avaliacao = avaliacaoItem,
                                onClick = { onMedidaClick(avaliacaoItem.medida.id) },
                                onDeleteClick = { viewModel.deleteMedida(avaliacaoItem.medida) }
                            )
                        }
                    }

                    // Espaço extra no final
                    item { Spacer(modifier = Modifier.height(64.dp)) } // Para não ficar colado no FAB
                }
            }
        }
    }
}

// Card com informações básicas do paciente
@Composable
fun PacienteInfoCard(paciente: Paciente, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Informações do Paciente", style = MaterialTheme.typography.titleSmall)
            Divider()
            InfoRow(label = "Nome", value = paciente.nome)
            InfoRow(label = "Data Nasc.", value = paciente.dataDeNascimento.format(DateTimeFormatter.toLocaleDateString()))
            InfoRow(label = "Idade", value = "${paciente.idade} anos")
            InfoRow(label = "Sexo", value = paciente.sexo.name.lowercase().replaceFirstChar { it.titlecase() })
            // TODO: Adicionar Altura e Peso da ÚLTIMA avaliação se desejado
        }
    }
}

// Linha de informação reutilizável
@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp)) // Largura fixa para alinhar
        Text(value)
    }
}


// Card para exibir um item da lista de avaliações
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliacaoItemCard(
    avaliacao: MedidaItemUiState,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medida = avaliacao.medida
    val gorduraFormatada = avaliacao.gorduraCalculada?.format(1) ?: "--"
    val imcFormatado = medida.imc?.format(1) ?: "--"
    val dataFormatada = medida.dataAvaliacao.format(DateTimeFormatter.toLocaleDateString())

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Avaliação de $dataFormatada",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Peso: ${medida.peso?.toString() ?: "--"} kg", style = MaterialTheme.typography.bodyMedium)
                    Text("IMC: $imcFormatado", style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = "%Gordura: $gorduraFormatada%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold // Destaca a gordura
                )
                Text(
                    text = "Protocolo: ${medida.protocoloUsado.name.replace(\"PROTOCOLO_\",\"\").replace(\"DOBRAS\", \" Dobras\")}", // Formata nome protocolo
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                            // Ícone de deletar
                            IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Excluir Avaliação",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
            }
        }
    }


    // Extension function para DateTimeFormatter (para simplificar)
    fun DateTimeFormatter.Companion.toLocaleDateString(): DateTimeFormatter {
        // Adapte o padrão conforme necessário para a localidade desejada
        return DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
    }

    // Extension function Double.format (se ainda não tiver em outro lugar)
    fun Double.format(digits: Int): String = "%.${digits}f".format(Locale.ROOT, this)


    // --- Preview ---
    @Preview(showBackground = true)
    @Composable
    fun PreviewPacienteDetailScreen() {
        CalculadoracorpoTheme {
            // Mock de dados para o preview
            val pacientePreview = Paciente(1, "Maria Preview", LocalDate.of(1995, 3, 10), Sexo.FEMININO)
            val avaliacoesPreview = listOf(
                MedidaItemUiState(
                    Medidas(1, 1, LocalDate.now().minusMonths(3), 165.0, 65.5, Protocolo.PROTOCOLO_3_DOBRAS, null,null, 18.0, null, null, 25.0, 22.0),
                    gorduraCalculada = 23.5
                ),
                MedidaItemUiState(
                    Medidas(2, 1, LocalDate.now(), 165.5, 66.8, Protocolo.PROTOCOLO_7_DOBRAS, 12.0, 22.0, 19.0, 15.0, 14.0, 26.0, 23.0),
                    gorduraCalculada = 22.1
                )
            )
            // O ideal seria mockar o ViewModel, mas para visualização simples:
            val mockUiState = PacienteDetailUiState(
                paciente = pacientePreview,
                avaliacoes = avaliacoesPreview,
                isLoading = false
            )

            Scaffold(topBar = { TopAppBar(title = { Text(pacientePreview.nome) }) }) { padding ->
                LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { PacienteInfoCard(paciente = mockUiState.paciente!!) }
                    item { Text("Histórico de Avaliações", style = MaterialTheme.typography.titleMedium) }
                    items(mockUiState.avaliacoes) { aval -> AvaliacaoItemCard(aval, {}, {}) }
                }
            }
        }
    }
