package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "analysis_request")
data class AnalysisRequestEntity(
    @PrimaryKey val id: Int,                 // identifiant de la demande (id_data)
    val recordId: Int?,                      // identifiant du dossier (id_dos)
    val analysisRef: Int?,                   // référence de l’analyse (ref_analyse)
    val price: Double?,                      // prix de l’analyse (prix)
    val isPaid: Int?,                        // état de paiement (paye)
    val isUrgent: Int?,                      // marqueur d’urgence (urgent)
    val requestType: Int?,                   // type de demande (demande)
    val isOutsourced: String?                // externalisé ou non (req_outsourced)
)
