package com.tm78775.retroforcetest

import com.google.gson.annotations.SerializedName

data class Technician(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String,
    @SerializedName("Identification_Number__c") val badgeId: String,
    var uid: String = ""
)