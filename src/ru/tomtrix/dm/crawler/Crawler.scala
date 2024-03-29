package ru.tomtrix.dm.crawler

import java.util.Date
import com.mongodb.casbah.Imports._
import org.dom4j.tree.DefaultElement
import ru.tomtrix.dm.crawler.Common._
import ru.tomtrix.xpathparser.XPathParser

/**
 * Crawler (http://sport.rbc.ru)
 * @author tom-trix
 */
object Crawler {
    /** Number of article from which we should crawl */
    val startArticle = 369500
    /** Number of article till which we should crawl */
    val endArticle = 371500

    /**
     * @param i - article number
     * @param xpath - xPath expression
     * @return list of parsed paragraphs
     */
    def parse(i: Int, xpath: String) = try {
        val x = XPathParser.getInstance.parseNodes("http://sport.rbc.ru/football/newsline/24/09/2012/" + i + ".shtml", xpath).toArray.toList map { _.asInstanceOf[DefaultElement].getText } filter (!_.trim.isEmpty)
        if (x(0).startsWith("Ресурс, который вы запросили, не найден на сервере")) None else Some(x)
    } catch { case e: Exception => None }

    def main(args: Array[String]): Unit = {
        // database manipulations
        mongoDocs.remove(MongoDBObject.empty)
        mongoDocs.ensureIndex("article")
        mongoDocs.ensureIndex("date")
        mongoKeywords.remove(MongoDBObject.empty)
        mongoKeywords.ensureIndex("keyword")
        mongoWords.remove(MongoDBObject.empty)
        mongoWords.ensureIndex("word")

        // launch the crawler
        val s = """//*[@id="window"]/div/div[2]/div[1]/div/div"""
        val xpath = List("[2]/*", "[3]/*", "[4]") map (s + _)
        for {
            i <- startArticle to endArticle
            docs <- parse(i, xpath(0)) 			//list of paragraphs of the article
            tags <- parse(i, xpath(1)) 			//list of tags
            prse <- parse(i, xpath(2)) 			//date
            date <- Some(prse(0) split (" "))	//split date
        } yield {
            // add the article to "documents"
            mongoDocs.insert(MongoDBObject("article" -> i, "doc" -> docs, "tags" -> tags, "date" -> new Date(date(2).toInt - 1900, months indexOf stem(date(1)), date(0) toInt)))
            // build a Reverse Index for words
            for {
                keyword <- tags map (_.trim toLowerCase) filter (!_.isEmpty)
            } yield mongoKeywords.update(MongoDBObject("keyword" -> stem(keyword)), $addToSet("docs" -> i), true, false)
            // build a Reverse Index for keywords
            for {
                sentence <- docs
                word <- sentence.split(splitRegex) map (_.trim toLowerCase) filter (t => !t.isEmpty && !stopWords.contains(t))
            } yield mongoWords.update(MongoDBObject("word" -> stem(word)), $addToSet("docs" -> i), true, false)
        }
        println("Done")
    }
}