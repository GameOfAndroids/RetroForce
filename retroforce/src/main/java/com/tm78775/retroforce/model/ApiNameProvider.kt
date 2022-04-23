package com.tm78775.retroforce.model

interface ApiNameProvider : Fetchable {
    fun getApiName(): String
}