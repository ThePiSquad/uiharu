package club.pisquad.uiharu.qqbot.websocket.exception

class ReconnectByServer : Exception("Reconnect by server")

class ReconnectError : Exception("Reconnect error")

class ReconnectResumed : Exception("Reconnect resumed")