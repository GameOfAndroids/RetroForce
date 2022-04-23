package com.tm78775.retroforce.service

data class DescribeResponse (
    var fields: List<Field>
)

data class Field(
    var name: String,
    val picklistValues: List<PicklistValue>
)

data class PicklistValue(
    val active: Boolean,
    val defaultValue: Boolean,
    val label: String,
    val value: String
)