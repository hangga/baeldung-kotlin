package com.baeldung.debugsuspendfunction

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DebugSuspendFunctionUnitTest {
    private val myList = listOf("Kotlin", "Java", "Python", "JavaScript")

    private suspend fun getElementAtIndex(list: List<String>, index: Int): String {
        return list[index]
    }

    private suspend fun updateElementAtIndex(list: MutableList<String>, index: Int, newValue: String) {
        list[index] = newValue
    }

    @Test
    fun `test suspend get element`() = runBlocking{
        val element1 = getElementAtIndex(myList, 5)
        println("Element at index 2: $element1")
    }
}