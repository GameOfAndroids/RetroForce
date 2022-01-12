package com.tm78775.retroforce.model

class UnauthenticatedException(val msg: String): Exception()
class RefreshException(val msg: String) : Exception()