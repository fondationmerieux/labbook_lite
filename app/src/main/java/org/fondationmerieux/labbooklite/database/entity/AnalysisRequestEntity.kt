package org.fondationmerieux.labbooklite.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "analysis_request") // sigl_04_data in LabBook database
data class AnalysisRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,                 // identifiant de la demande (id_data)
    val recordId: Int?,                      // identifiant du dossier (id_dos)
    val analysisRef: Int?,                   // référence de l’analyse (ref_analyse)
    val isUrgent: Int?,                      // marqueur d’urgence (urgent)
    val ana_req_user: Int = 0
)
