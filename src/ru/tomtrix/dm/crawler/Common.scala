package ru.tomtrix.dm.crawler

import com.mongodb.casbah.MongoConnection
import org.tartarus.snowball.ext.russianStemmer

/**
 * @author tom-trix
 */
object Common {
    val stemmer = new russianStemmer
    val stopWords = Set("а", "без", "более", "бы", "был", "была", "были", "было", "быть", "в", "вам", "вас", "весь", "во", "вот", "все", "всего", "всех", "вы", "где", "да", "даже", "для", "до", "его", "ее", "если", "есть", "еще", "же", "за", "здесь", "и", "из", "или", "им", "их", "к", "как", "ко", "когда", "кто", "ли", "либо", "мне", "может", "мы", "на", "надо", "наш", "не", "него", "нее", "нет", "ни", "них", "но", "ну", "о", "об", "однако", "он", "она", "они", "оно", "от", "очень", "по", "под", "при", "с", "со", "так", "также", "такой", "там", "те", "тем", "то", "того", "тоже", "той", "только", "том", "ты", "у", "уже", "хотя", "чего", "чей", "чем", "что", "чтобы", "чье", "чья", "эта", "эти", "это", "я")
    val mongoDocs = MongoConnection("localhost")("TrixCrawler")("documents")
    val mongoWords = MongoConnection("localhost")("TrixCrawler")("words")
    val splitRegex = "[\\. \n\r\t)(;,:\"-?!]"

    def stem(word: String) = {
        stemmer setCurrent (word)
        stemmer.stem
        stemmer.getCurrent
    }
}