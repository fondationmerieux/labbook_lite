package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "analysis_result")
data class AnalysisResultEntity(
    @PrimaryKey val id: Int,              // identifiant du résultat (id_data)
    val analysisId: Int,                  // identifiant de la demande d’analyse (id_analyse)
    val variableRef: Int,                 // identifiant de la variable (ref_variable)
    val value: String?,                   // valeur du résultat (valeur)
    val isRequired: Int?,                // champ obligatoire (obligatoire)
    val resultRecovery: String           // statut de récupération du résultat (res_recovery)
)
