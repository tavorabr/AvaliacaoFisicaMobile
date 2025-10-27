package com.example.calculadoracorpo.data.model

data class ResultadoAvaliacao(
    val avaliacaoId: Long,
    val pacienteId: Long,
    val dataAvaliacao: String,

    val imc: Double,
    val classificacaoImc: String,

    val densidadeCorporal: Double,
    val percentualGordura: Double,
    val massaGordaKg: Double,
    val massaMagraKg: Double,

    val protocoloUtilizado: String
)