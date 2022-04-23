package com.tm78775.retroforce.service

class UnauthenticatedException(val msg: String): Exception()
class RefreshException(val msg: String) : Exception()
class SessionExpiredException(val msg: String) : Exception()
class ServerUnsuccessfulException(val msg: String) : Exception()
class ServerNotConfigured(val msg: String) : Exception()
class AuthTokenException(val msg: String) : Exception()
class MissingSfidException(val msg: String = "The object does not have the " +
        "required sfid for this operation.") : Exception()