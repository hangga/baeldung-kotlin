package com.baeldung.sentencereverse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ReverseSentenceUnitTest {

    @Test
    fun `reverse sentence using programmatic approach`() {
        val sentence = "this is a complete sentence"
        assertEquals("sentence complete a is this", reverseWordsInSentenceCustomMethod(sentence))
    }

    @Test
    fun `reverse sentence using split and joinToString methods`() {
        val sentence = "this is a complete sentence"
        assertEquals("sentence complete a is this", reverseSentenceUsingReverseAndJoinToStringMethods(sentence))
    }

    @Test
    fun `reverse sentence using a fold method`() {
        val sentence = "this is a complete sentence"
        assertEquals("sentence complete a is this", reverseSentenceUsingFoldMethod(sentence))
    }

    @Test
    fun `reverse sentence using a stack`() {
        val sentence = "this is a complete sentence"
        assertEquals("sentence complete a is this", reverseWordsInSentenceUsingStack(sentence))
    }

    fun reverseSentenceUsingReverseAndJoinToStringMethods(sentence: String): String {
        val words = sentence.split(" ")
        return words.reversed().joinToString(" ")
    }

    fun reverseWordsInSentenceCustomMethod(sentence: String): String {
        val words = sentence.split(" ")

        val reversedStringBuilder = StringBuilder()

        for (word in words) {
            reversedStringBuilder.insert(0, "$word ")
        }

        return reversedStringBuilder.toString().trim()
    }

    fun reverseWordsInSentenceUsingStack(sentence: String): String {
        val stack = Stack<String>()
        val words = sentence.split(" ")
        for (word in words) {
            stack.push(word)
        }
        val reversedSentence = StringBuilder()
        while (!stack.empty()) {
            reversedSentence.append("${stack.pop()} ")
        }
        return reversedSentence.toString().trim()
    }

    fun reverseSentenceUsingFoldMethod(sentence: String): String {
        return sentence.split(" ")
            .fold("") { acc, word -> "$word $acc" }
            .trim()
    }
}