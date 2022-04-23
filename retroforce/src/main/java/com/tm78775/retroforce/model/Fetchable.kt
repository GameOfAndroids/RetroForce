package com.tm78775.retroforce.model

interface Fetchable {
    var sfid: String

    /**
     * Helper method to determine if sfid has a value.
     * @return True if [sfid] is not empty and not blank. Otherwise false is returned.
     */
    fun hasSfid(): Boolean {
        return sfid.isNotBlank()
    }

    /**
     * Helper method to determine if sfid has a value.
     * @return True if the [sfid] is empty or blank. Otherwise false is returned.
     */
    fun hasNoSfid(): Boolean {
        return sfid.isBlank()
    }
}