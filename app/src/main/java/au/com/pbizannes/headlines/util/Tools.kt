package au.com.pbizannes.headlines.util

import java.time.Instant

object Tools {
    fun now(): Instant = Instant.now()

    fun then(date: String): Instant = Instant.parse(date)
}