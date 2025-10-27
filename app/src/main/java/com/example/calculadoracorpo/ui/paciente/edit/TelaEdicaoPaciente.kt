package com.example.calculadoracorpo.ui.paciente.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check // Ícone Salvar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.di.AppViewModelProvider
import com.example.calculadoracorpo.ui.screens.SexoDropdown // Reutiliza o SexoDropdown
import com.example.calculadoracorpo.ui.theme.CalculadoracorpoTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacienteEditScreen(
    onSaveComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PacienteEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    // Efeito para navegar de volta após salvar com sucesso
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveComplete()
            viewModel.resetSaveSuccess() // Reseta o flag
        }
    }

    // Efeito para mostrar Snackbar em caso de erro
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("Erro: $it")
            // TODO: Adicionar lógica para limpar o erro no ViewModel após mostrar
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id == null) "Adicionar Paciente" else "Editar Paciente") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Botão Salvar na AppBar
                    IconButton(onClick = viewModel::savePaciente, enabled = uiState.isFormValid && !uiState.isLoading) {
                        Icon(Icons.Filled.Check, contentDescription = "Salvar")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->

        // Mostra loading overlay
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Só mostra o formulário se os dados iniciais foram carregados (ou se for adição)
        if (uiState.isPacienteLoaded) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = viewModel::onNomeChange,
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = uiState.dataNascimentoString,
                    onValueChange = viewModel::onDataNascimentoChange,
                    label = { Text("Data Nascimento (dd/MM/yyyy)") },
                    placeholder = { Text("ex: 25/12/1990") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    visualTransformation = DateVisualTransformation(), // Ajuda na formatação
                    enabled = !uiState.isLoading
                )

                SexoDropdown(
                    selectedSexo = uiState.sexo,
                    onSexoSelected = viewModel::onSexoChange,
                    modifier = Modifier.fillMaxWidth()
                    // enabled = !uiState.isLoading // Dropdown não tem 'enabled' direto, controlar via overlay talvez
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botão Salvar no final (alternativa/redundante ao da AppBar)
                Button(
                    onClick = viewModel::savePaciente,
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Paciente")
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("Cancelar")
                }

            }
        } else if (!uiState.isLoading && !uiState.isPacienteLoaded && uiState.errorMessage != null) {
            // Caso de erro ao carregar paciente para edição
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Não foi possível carregar os dados do paciente.", color = MaterialTheme.colorScheme.error)
            }
        }
        // Se isLoading = true e isPacienteLoaded = false, o indicador de loading já está sendo mostrado.
    }
}

// Visual Transformation simples para data (apenas adiciona barras) - pode ser melhorado
class DateVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "/" // Adiciona / depois do 2º e 4º dígito
        }

        val numberOffsetTranslator = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return 10 // Limita o tamanho
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset -1
                if (offset <= 10) return offset -2
                return 8 // Limita o tamanho
            }
        }

        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), numberOffsetTranslator)
    }
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun PreviewPacienteEditScreen_Add() {
    CalculadoracorpoTheme {
        // Simula estado inicial de adição
        PacienteEditScreen(onSaveComplete = {}, onCancel = {})
        // O preview usará um ViewModel real, mas sem SavedStateHandle,
        // então iniciará em modo de adição.
    }
}
