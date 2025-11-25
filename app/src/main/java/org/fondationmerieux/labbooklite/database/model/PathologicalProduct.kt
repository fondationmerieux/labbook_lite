package org.fondationmerieux.labbooklite.database.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PathologicalProduct(
    val analysisId: Int,
    val analysisCode: String,
    sampleType: Int,
    productType: Int,
    prelDate: String,
    prelTime: String,
    code: String,
    recvDate: String,
    recvTime: String,
    status: Int
) {
    var sampleType by mutableIntStateOf(sampleType)
    var productType by mutableIntStateOf(productType)
    var prelDate by mutableStateOf(prelDate)
    var prelTime by mutableStateOf(prelTime)
    var code by mutableStateOf(code)
    var recvDate by mutableStateOf(recvDate)
    var recvTime by mutableStateOf(recvTime)
    var status by mutableIntStateOf(status)

    var isStatusMenuExpanded: MutableState<Boolean> = mutableStateOf(false)
}