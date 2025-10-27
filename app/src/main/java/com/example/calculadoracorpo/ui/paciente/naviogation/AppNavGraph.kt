package com.example.calculadoracorpo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.calculadoracorpo.ui.paciente.detail.PacienteDetailScreen
import com.example.calculadoracorpo.ui.paciente.edit.PacienteEditScreen
import com.example.calculadoracorpo.ui.paciente.list.TelaListaPacientes
// Importe as telas de Medidas aqui quando criá-las

// Define as rotas como constantes
object Routes {
    const val PACIENTE_LIST = "pacienteList"
    const val PACIENTE_DETAIL = "pacienteDetail" // Precisa de ID
    const val PACIENTE_EDIT = "pacienteEdit"   // Pode precisar de ID (para editar)
    const val PACIENTE_ADD = "pacienteAdd"     // Rota específica para adicionar

    // Argumentos
    const val ARG_PACIENTE_ID = "pacienteId"

    // Rotas completas com argumentos
    fun pacienteDetailRoute(pacienteId: Int) = "$PACIENTE_DETAIL/$pacienteId"
    fun pacienteEditRoute(pacienteId: Int) = "$PACIENTE_EDIT/$pacienteId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PACIENTE_LIST, // Tela inicial
        modifier = modifier
    ) {
        // --- Tela Lista de Pacientes ---
        composable(route = Routes.PACIENTE_LIST) {
            TelaListaPacientes(
                onPacienteClick = { pacienteId ->
                    navController.navigate(Routes.pacienteDetailRoute(pacienteId))
                },
                onAddPacienteClick = {
                    navController.navigate(Routes.PACIENTE_ADD) // Navega para adicionar
                }
            )
        }

        // --- Tela Adicionar Paciente ---
        composable(route = Routes.PACIENTE_ADD) {
            PacienteEditScreen(
                onSaveComplete = { navController.popBackStack() }, // Volta após salvar
                onCancel = { navController.popBackStack() }      // Volta ao cancelar
                // pacienteId será nulo aqui (modo de adição)
            )
        }

        // --- Tela Editar Paciente ---
        composable(
            route = "${Routes.PACIENTE_EDIT}/{${Routes.ARG_PACIENTE_ID}}",
            arguments = listOf(navArgument(Routes.ARG_PACIENTE_ID) { type = NavType.IntType })
        ) {
            // O ID é obtido automaticamente pelo ViewModel via SavedStateHandle
            PacienteEditScreen(
                onSaveComplete = { navController.popBackStack(Routes.PACIENTE_LIST, false) }, // Volta para a lista
                onCancel = { navController.popBackStack() } // Volta para a tela anterior (detalhes)
            )
        }

        // --- Tela Detalhes do Paciente ---
        composable(
            route = "${Routes.PACIENTE_DETAIL}/{${Routes.ARG_PACIENTE_ID}}",
            arguments = listOf(navArgument(Routes.ARG_PACIENTE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            // O ID é obtido automaticamente pelo ViewModel via SavedStateHandle
            val pacienteId = backStackEntry.arguments?.getInt(Routes.ARG_PACIENTE_ID) ?: -1 // Segurança
            PacienteDetailScreen(
                onEditClick = { navController.navigate(Routes.pacienteEditRoute(pacienteId)) },
                onAddMedidaClick = { /* TODO: Navegar para TelaEdicaoMedidas */ },
                onMedidaClick = { medidaId -> /* TODO: Navegar para TelaDetalhesMedidas */ },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Telas de Medidas (Futuro) ---
        /*
        composable(...) { TelaEdicaoMedidas(...) }
        composable(...) { TelaDetalhesMedidas(...) }
        */
    }
}
