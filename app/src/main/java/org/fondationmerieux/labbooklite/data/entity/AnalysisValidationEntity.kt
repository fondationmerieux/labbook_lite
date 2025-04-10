package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "analysis_validation")
data class AnalysisValidationEntity(
    @PrimaryKey val id: Int,                 // identifiant de la validation (id_data)
    val resultId: Int?,                      // identifiant du résultat validé (id_resultat)
    val validationDate: String?,             // date de validation (date_validation)
    val userId: Int,                         // identifiant de l'utilisateur (utilisateur)
    val value: String?,                      // valeur saisie lors de la validation (valeur)
    val validationType: Int?,                // type de validation (type_validation)
    val comment: String?,                    // commentaire libre (commentaire)
    val cancelReason: Int?                   // motif d’annulation (motif_annulation)
)
