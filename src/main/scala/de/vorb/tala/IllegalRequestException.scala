package de.vorb.tala

class IllegalRequestException extends Exception {
    override def getMessage: String = "Illegal request"
}
