package org.fondationmerieux.labbooklite.data.model

import org.fondationmerieux.labbooklite.data.entity.*

/**
 * Created by AlC on 31/03/2025.
 */
data class SetupResponse(
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
    val logo_base64: String? = null
)