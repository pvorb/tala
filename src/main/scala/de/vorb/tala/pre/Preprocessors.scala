package de.vorb.tala.pre

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions

object Preprocessors {
    val pegdown: PegDownProcessor =
        new PegDownProcessor(Extensions.SMARTS | Extensions.HARDWRAPS |
            Extensions.SMARTS | Extensions.SUPPRESS_ALL_HTML)
}
