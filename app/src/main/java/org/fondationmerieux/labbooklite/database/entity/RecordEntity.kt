package org.fondationmerieux.labbooklite.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "record")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id_data: Int,        // identifiant du dossier
    val patient_id: Int?,                // patient lié
    val type: Int?,                      // type de dossier
    val rec_date_receipt: String?,       // date de réception
    val prescriber: Int?,                // médecin prescripteur
    val prescription_date: String?,      // date de prescription
    val report: String?,                 // compte-rendu clinique
    val status: Int?,                    // statut du dossier
    val rec_num_int: String?,            // numéro interne
    val rec_date_vld: String?,           // date validation
    val rec_modified: String?,           // modification
    val rec_hosp_num: String?,           // numéro hospitalisation
    val rec_date_save: String?,          // date enregistrement
    val rec_num_lite: String?,
    val rec_lite: Int
)