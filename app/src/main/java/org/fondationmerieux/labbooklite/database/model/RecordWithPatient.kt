package org.fondationmerieux.labbooklite.database.model

import androidx.room.Embedded
import androidx.room.Relation
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity

data class RecordWithPatient(
    @Embedded val record: RecordEntity,

    @Relation(
        parentColumn = "patient_id",
        entityColumn = "id_data"
    )
    val patient: PatientEntity?
)
