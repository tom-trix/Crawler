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

    def main(args: Array[String]): Unit = {
        //манипуляции с БД
        mongoDocs.remove(MongoDBObject.empty)
        mongoDocs.ensureIndex("article")
        mongoDocs.ensureIndex("date")
        mongoKeywords.remove(MongoDBObject.empty)
        mongoKeywords.ensureIndex("keyword")
        mongoWords.remove(MongoDBObject.empty)
        mongoWords.ensureIndex("word")

        //запускаем паука
        for {
            i <- startArticle to endArticle
            s <- Some("""//*[@id="window"]/div/div[2]/div[1]/div/div""")
            all <- try {
                Some(for {
                    //во 2-м диве содержится статья, в 3-м - теги, в 4-м - дата публикации
                    xpath <- List("[2]/*", "[3]/*", "[4]") map (s + _)
                    pr <- Some(XPathParser.getInstance.parseNodes("http://sport.rbc.ru/football/newsline/24/09/2012/" + i + ".shtml", xpath).toArray map { _.asInstanceOf[DefaultElement].getText } filter (!_.trim.isEmpty))
                } yield pr)
            } catch { case e: Exception => None }
            docs <- if (all(0)(0).startsWith("Ресурс, который вы запросили, не найден на сервере")) None else Some(all)
            date <- Some(docs(2)(0) split " ") 
        } yield {
            //добавляем статью в коллекцию документов
            mongoDocs.insert(MongoDBObject("article" -> i, "doc" -> docs(0), "tags" -> docs(1), "date" -> new Date(date(2).toInt - 1900, months indexOf stem(date(1)), date(0) toInt)))
            //строим обратный индекс для ключевых слов
            for {
                keyword <- docs(1) map (_.trim toLowerCase) filter (!_.isEmpty)
            } yield mongoKeywords.update(MongoDBObject("keyword" -> stem(keyword)), $addToSet("docs" -> i), true, false)
            //строим обратный индекс для всех слов
            for {
                sentence <- docs(0)
                word <- sentence.split(splitRegex) map (_.trim toLowerCase) filter (t => !t.isEmpty && !stopWords.contains(t))
            } yield mongoWords.update(MongoDBObject("word" -> stem(word)), $addToSet("docs" -> i), true, false)
        }
        println("Done")
    }
}