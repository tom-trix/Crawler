/**
 * === Libraries for MongoDB ===
 * casbah-commons
 * casbah-core
 * casbah-gridfs
 * casbah-query
 * scalaj-collection
 * mongo-java-driver
 * joda-time
 * slf4j-api
 */
package ru.tomtrix.dm.crawler

import ru.tomtrix.dm.crawler.Common._
import com.mongodb.casbah.Imports._
import ru.tomtrix.xpathparser.XPathParser
import org.dom4j.tree.DefaultElement

/**
 * @author tom-trix
 */
object Crawler {
    /** Number of article from which we should crawl */
    val startArticle = 370000
    /** Number of article till which we should crawl */
    val endArticle = 371000

    def main(args: Array[String]): Unit = {
        //manipulate with MongoDB (remove all data and create indexes)
        mongoDocs.remove(MongoDBObject.empty)
        mongoDocs.ensureIndex("article")
        mongoWords.remove(MongoDBObject.empty)
        mongoWords.ensureIndex("word")

        //launch the crawling
        for {
            i <- startArticle to endArticle
            preliminary <- try { Some(XPathParser.getInstance.parseNodes("http://sport.rbc.ru/football/newsline/24/09/2012/" + i + ".shtml", """//*[@id="window"]/div/div[2]/div[1]/div/div[2]/*""").toArray map { _.asInstanceOf[DefaultElement].getText } filter (!_.trim.isEmpty)) } catch { case e: Exception => None }
            doc <- if (preliminary(0).startsWith("Ресурс, который вы запросили, не найден на сервере")) None else Some(preliminary)
        } yield {
            mongoDocs.insert(MongoDBObject("article" -> i, "doc" -> doc))
            for {
                sentence <- doc
                word <- sentence.split(splitRegex) map (_.toLowerCase) filter (t => !t.trim.isEmpty && !stopWords.contains(t))
            } yield mongoWords.update(MongoDBObject("word" -> stem(word)), $addToSet("docs" -> i), true, false)
        }
        println("Done")
    }
}