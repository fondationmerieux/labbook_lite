package org.fondationmerieux.labbooklite.database.model

import org.fondationmerieux.labbooklite.database.entity.AnaLinkEntity
import org.fondationmerieux.labbooklite.database.entity.AnaVarEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisRequestEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisResultEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisValidationEntity
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.NationalityEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.PreferencesEntity
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.database.entity.SampleEntity
import org.fondationmerieux.labbooklite.database.entity.UserEntity

/**
 * Created by AlC on 31/03/2025.
 */
data class SetupResponse(
    val lite_ser: Int,
    val lite_name: String,
    val users: List<UserEntity>,
    val patients: List<PatientEntity>,
    val preferences: List<PreferencesEntity>,
    val nationality: List<NationalityEntity>,
    val dictionary: List<DictionaryEntity>,
    val analysis: List<AnalysisEntity>,
    val ana_link: List<AnaLinkEntity>,
    val ana_var: List<AnaVarEntity>,
    val sample: List<SampleEntity>,
    val record: List<RecordEntity>,
    val analysis_request: List<AnalysisRequestEntity>,
    val analysis_result: List<AnalysisResultEntity>,
    val analysis_validation: List<AnalysisValidationEntity>,
    val prescribers: List<PrescriberEntity>,
    val logo_base64: String? = null
)