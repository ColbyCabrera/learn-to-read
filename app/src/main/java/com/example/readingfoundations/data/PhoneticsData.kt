package com.example.readingfoundations.data

data class Phoneme(
    val grapheme: String, // The letter(s) that represent the sound
    val ipaSymbol: String, // The International Phonetic Alphabet symbol
    val exampleWord: String
)

object PhoneticsData {
    val phoneticPronunciations = mapOf(
        "A" to "ah",
        "B" to "buh",
        "C" to "kuh",
        "D" to "duh",
        "E" to "eh",
        "F" to "fff",
        "G" to "guh",
        "H" to "huh",
        "I" to "ih",
        "J" to "juh",
        "K" to "kuh",
        "L" to "luh",
        "M" to "mmm",
        "N" to "nnn",
        "O" to "ah",
        "P" to "puh",
        "Q" to "kwuh",
        "R" to "ruh",
        "S" to "sss",
        "T" to "tuh",
        "U" to "uh",
        "V" to "vvv",
        "W" to "wuh",
        "X" to "ks",
        "Y" to "yuh",
        "Z" to "zzz"
    )

    val phonemes = listOf(
        Phoneme("A", "/æ/", "Apple"),
        Phoneme("B", "/b/", "Ball"),
        Phoneme("C", "/k/", "Cat"),
        Phoneme("D", "/d/", "Dog"),
        Phoneme("E", "/ɛ/", "Egg"),
        Phoneme("F", "/f/", "Fish"),
        Phoneme("G", "/ɡ/", "Goat"),
        Phoneme("H", "/h/", "Hat"),
        Phoneme("I", "/ɪ/", "Igoo"),
        Phoneme("J", "/dʒ/", "Jam"),
        Phoneme("K", "/k/", "Kite"),
        Phoneme("L", "/l/", "Lion"),
        Phoneme("M", "/m/", "Monkey"),
        Phoneme("N", "/n/", "Nest"),
        Phoneme("O", "/ɒ/", "Octopus"),
        Phoneme("P", "/p/", "Pig"),
        Phoneme("Q", "/kw/", "Queen"),
        Phoneme("R", "/r/", "Rabbit"),
        Phoneme("S", "/s/", "Sun"),
        Phoneme("T", "/t/", "Turtle"),
        Phoneme("U", "/ʌ/", "Up"),
        Phoneme("V", "/v/", "Vase"),
        Phoneme("W", "/w/", "Whale"),
        Phoneme("X", "/ks/", "X-ray"),
        Phoneme("Y", "/j/", "Yo-yo"),
        Phoneme("Z", "/z/", "Zebra")
    )
}