package com.tm78775.retroforce.model

import com.google.gson.annotations.SerializedName

data class Attributes(
    @SerializedName("type") var type: String,
    @SerializedName("url") var url: String
)
