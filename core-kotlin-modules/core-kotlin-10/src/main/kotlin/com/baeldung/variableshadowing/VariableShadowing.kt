package com.baeldung.variableshadowing

class Car {
    val speed: Int = 100

    fun upSpeed() : Int {
        val speed = speed * 2 // Shadowing the constructor parameter 'speed'
        return speed
    }
}

val name = "Budi"

fun getNameShadow(): String {
    val name = "Ani"
    return name
}

fun getName(): String {
    return name
}
