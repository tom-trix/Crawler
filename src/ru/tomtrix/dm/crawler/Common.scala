/**
 * ========= Description =========
 * this project combines:
 * 1) crawler (to MongoDB)
 * 2) searcher (based on MongoDB)
 * 3) data importer (MongoDB -> Redis)
 * 4) speed tester (MongoDB vs. Redis)
 * 
 * ========== Libraries ==========
 * casbah-commons		=> MongoDB
 * casbah-core			=> MongoDB
 * casbah-gridfs		=> MongoDB
 * casbah-query			=> MongoDB
 * scalaj-collection	=> MongoDB
 * mongo-java-driver	=> MongoDB
 * joda-time			=> MongoDB
 * slf4j-api			=> MongoDB
 * jedis				=> RedisDB
 * snowball				=> Snowball
 * xpathparser			=> My xPath-Wrapper, based on jaxen, cyberneko, dom4j & xerces
 */
package ru.tomtrix.dm.crawler

import com.mongodb.casbah.MongoConnection
import org.tartarus.snowball.ext.russianStemmer

/**
 * Common resources
 * @author tom-trix
 */
object Common {
    val stemmer = new russianStemmer
    val stopWords = Set("а", "без", "более", "бы", "был", "была", "были", "было", "быть", "в", "вам", "вас", "весь", "во", "вот", "все", "всего", "всех", "вы", "где", "да", "даже", "для", "до", "его", "ее", "если", "есть", "еще", "же", "за", "здесь", "и", "из", "или", "им", "их", "к", "как", "ко", "когда", "кто", "ли", "либо", "мне", "может", "мы", "на", "надо", "наш", "не", "него", "нее", "нет", "ни", "них", "но", "ну", "о", "об", "однако", "он", "она", "они", "оно", "от", "очень", "по", "под", "при", "с", "со", "так", "также", "такой", "там", "те", "тем", "то", "того", "тоже", "той", "только", "том", "ты", "у", "уже", "хотя", "чего", "чей", "чем", "что", "чтобы", "чье", "чья", "эта", "эти", "это", "я")
    val months = List("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь") map stem
    val host = "localhost"
    val db = "TrixCrawler"
    val mongoDocs = MongoConnection(host)(db)("documents")
    val mongoKeywords = MongoConnection(host)(db)("keywords")
    val mongoWords = MongoConnection(host)(db)("words")
    val splitRegex = "[\\. \n\r\t)(;,:\"–—?!\\-]"
    val splitRegexWithoutExc = "[\\. \n\r\t)(;,:\"–—?\\-]"

    /** @param word
     * @return stemmed word (яблоками -> яблок) */
    def stem(word: String) = {
        stemmer setCurrent (word)
        stemmer.stem
        stemmer.getCurrent
    }
}