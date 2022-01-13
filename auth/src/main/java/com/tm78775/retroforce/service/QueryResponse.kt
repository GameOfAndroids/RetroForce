package com.tm78775.retroforce.service

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

class QueryResponse {
    @SerializedName("totalSize") var size: Int = 0
    @SerializedName("done") var done: Boolean = false
    @SerializedName("records") var records: JsonArray = JsonArray()
}