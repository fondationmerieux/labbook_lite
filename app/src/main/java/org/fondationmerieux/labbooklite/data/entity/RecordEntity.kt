package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "record")
data class RecordEntity(
    @PrimaryKey val id_data: Int,        // identifiant du dossier
    val patient_id: Int?,                // patient lié
    val type: Int?,                      // type de dossier
    val rec_date_receipt: String?,       // date de réception
    val day_record_number: String?,      // numéro du dossier du jour
    val year_record_number: String?,     // numéro du dossier de l'année
    val prescriber: Int?,                // médecin prescripteur
    val prescription_date: String?,      // date de prescription
    val internal_service: String?,       // service interne
    val bed_number: Int?,                // numéro de lit
    val parcel_id: String?,              // identifiant du colis
    val rec_parcel_date: String?,        // date de réception du colis
    val report: String?,                 // compte-rendu clinique
    val parcel_type: Int?,               // type de colis
    val price: Double?,                  // prix
    val discount_type: Int?,             // type de remise
    val discount_percent: Double?,       // pourcentage de remise
    val insurance_percent: Double?,      // pourcentage pris en charge assurance
    val amount_due: Double?,             // montant à payer
    val receipt_number: String?,         // numéro de quittance
    val invoice_number: String?,         // numéro de facture
    val status: Int?,                    // statut du dossier
    val month_record_number: String?,    // numéro de dossier du mois
    val hospitalization_date: String?,   // date d'hospitalisation
    val rec_custody: String?,            // garde
    val rec_num_int: String?,            // numéro interne
    val rec_date_vld: String?,           // date validation
    val rec_modified: String?,           // modification
    val rec_hosp_num: String?,           // numéro hospitalisation
    val rec_date_save: String?           // date enregistrement
)
