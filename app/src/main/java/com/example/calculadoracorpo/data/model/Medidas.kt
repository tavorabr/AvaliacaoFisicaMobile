package com.example.calculadoracorpo.data.model

// [Os comentários Javadoc foram omitidos para focar no código]

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medidas",
    foreignKeys = [ForeignKey(entity = Paciente::class, parentColumns = ["id"],
        childColumns = ["pacienteId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("pacienteId")]
)
data class Medidas (
    @PrimaryKey val id:Int,
    val pacienteId: Int,
    val dataAvaliacao: LocalDate,

    // Altura e peso são os dados de entrada principais, mantidos como Double?
    val altura: Double?,
    val peso: Double?,

    // O Protocolo é um ENUM ou Data Class, assumindo Protocolo já está definido
    val protocoloUsado: Protocolo,

    // Dobras Cutâneas - Manter Nulável no banco de dados (Room)
    val peitoral: Double?,
    val abdominal: Double?,
    val triceps: Double?, // Nota: Renomeado para 'triceps' no seu Repositório anterior, mas mantido aqui como 'triceps'
    val axilarMedia: Double?,
    val subescapular: Double?,
    val supraIliaca: Double?,
    val coxa: Double?,
) {
    // Propriedade calculada do IMC (Ok, usa Elvis para prevenir 0.0)
    val imc: Double?
        get(){
            if (peso == null || altura == null || altura == 0.0) return null
            val alturaEmMetros = altura/100.0
            return peso/(alturaEmMetros * alturaEmMetros)
        }

    // NOVA PROPRIEDADE CALCULADA: Para somar as dobras com segurança (0.0 se for null)
    // Usada no Repositório.
    val somaDobrasNaoNula: Double
        get() = (peitoral ?: 0.0) +
                (axilarMedia ?: 0.0) +
                (triceps ?: 0.0) +
                (subescapular ?: 0.0) +
                (abdominal ?: 0.0) +
                (supraIliaca ?: 0.0) +
                (coxa ?: 0.0)
}