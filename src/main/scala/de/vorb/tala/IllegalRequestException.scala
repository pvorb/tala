package de.vorb.tala

class IllegalRequestException(msg: String = "Illegal request")
        extends Exception {
    override def getMessage: String = msg
}
