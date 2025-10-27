package com.example.calculadoracorpo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.ui.medidas.TelaEntradaMedidas
import com.example.calculadoracorpo.ui.paciente.detail.PacienteDetailScreen
import com.example.calculadoracorpo.ui.paciente.edit.PacienteEditScreen
import com.example.calculadoracorpo.ui.paciente.list.TelaListaPacientes
import com.example.calculadoracorpo.ui.resultado.TelaResultado
import com.example.calculadoracorpo.ui.resultado.ResultadoViewModel

object Routes {
    const val PACIENTE_LIST = "pacienteList"
    const val PACIENTE_DETAIL = "pacienteDetail"
    const val PACIENTE_EDIT = "pacienteEdit"
    const val PACIENTE_ADD = "pacienteAdd"
    const val MEDIDAS_ENTRY = "medidasEntry"
    const val RESULTADO_AVALIACAO = "resultadoAvaliacao"

    const val ARG_PACIENTE_ID = "pacienteId"

    fun pacienteDetailRoute(pacienteId: Int) = "$PACIENTE_DETAIL/$pacienteId"
    fun pacienteEditRoute(pacienteId: Int) = "$PACIENTE_EDIT/$pacienteId"
    fun medidasEntryRoute(pacienteId: Int) = "$MEDIDAS_ENTRY/$pacienteId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PACIENTE_LIST,
        modifier = modifier
    ) {
        composable(route = Routes.PACIENTE_LIST) {
            TelaListaPacientes(
                onPacienteClick = { pacienteId ->
                    navController.navigate(Routes.pacienteDetailRoute(pacienteId))
                },
                onAddPacienteClick = {
                    navController.navigate(Routes.PACIENTE_ADD)
                }
            )
        }

        composable(route = Routes.PACIENTE_ADD) {
            PacienteEditScreen(
                onSaveComplete = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.PACIENTE_EDIT}/{${Routes.ARG_PACIENTE_ID}}",
            arguments = listOf(navArgument(Routes.ARG_PACIENTE_ID) { type = NavType.IntType })
        ) {
            PacienteEditScreen(
                onSaveComplete = { navController.popBackStack(Routes.PACIENTE_LIST, false) },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.PACIENTE_DETAIL}/{${Routes.ARG_PACIENTE_ID}}",
            arguments = listOf(navArgument(Routes.ARG_PACIENTE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getInt(Routes.ARG_PACIENTE_ID) ?: -1
            PacienteDetailScreen(
                onEditClick = { navController.navigate(Routes.pacienteEditRoute(pacienteId)) },
                onAddMedidaClick = { navController.navigate(Routes.medidasEntryRoute(pacienteId)) },
                onMedidaClick = { /* Implementar navegação para detalhes de medida */ },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Nova Tela: Entrada de Medidas (MedidasEntry) ---
        composable(
            route = "${Routes.MEDIDAS_ENTRY}/{${Routes.ARG_PACIENTE_ID}}",
            arguments = listOf(navArgument(Routes.ARG_PACIENTE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getInt(Routes.ARG_PACIENTE_ID) ?: -1

            // A ViewModel de Resultado é usada para DISPARAR o cálculo e carregar a TelaResultado
            val resultadoViewModel: ResultadoViewModel = viewModel(factory = AppViewModelProvider.Factory)

            TelaEntradaMedidas(
                pacienteId = pacienteId,
                resultadoViewModel = resultadoViewModel,
                onSaveAndNavigate = {
                    navController.navigate(Routes.RESULTADO_AVALIACAO)
                },
                onCancel = { navController.popBackStack() }
            )
        }

        // --- Nova Tela: Resultado da Avaliação (TelaResultado) ---
        composable(route = Routes.RESULTADO_AVALIACAO) {
            // Reutiliza a mesma ViewModel que foi preenchida na TelaEntradaMedidas
            TelaResultado(
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }
    }
}