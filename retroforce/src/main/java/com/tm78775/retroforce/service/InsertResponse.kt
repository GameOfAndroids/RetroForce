package com.tm78775.retroforce.service

import com.google.gson.annotations.SerializedName

data class InsertResponse(
    @SerializedName("id") val id: String,
    @SerializedName("errors") val errors: List<Any>,
    @SerializedName("success") var success: Boolean
)
