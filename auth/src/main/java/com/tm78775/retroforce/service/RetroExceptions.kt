package com.tm78775.retroforce.service

class UnauthenticatedException(val msg: String): Exception()
class RefreshException(val msg: String) : Exception()
class SessionExpiredException(val msg: String) : Exception()
class ServerUnsuccessfulException(val msg: String) : Exception()