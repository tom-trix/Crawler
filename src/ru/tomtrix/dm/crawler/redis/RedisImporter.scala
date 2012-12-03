package ru.tomtrix.dm.crawler.redis

import redis.clients.jedis.Jedis
import ru.tomtrix.dm.crawler.Common._
import com.mongodb.BasicDBList

/**
 * @author tom-trix
 * This object just fill Redis with data read from MongoDB<br>
 * Default DB is for articles, first DB is for Reverse Index
 */
object Redis {
    def main(args: Array[String]): Unit = {
        //connect
        val redis = new Jedis("localhost")
        
        //fill DB №0: Article_number => Article
        val docs = for {
            bson <- mongoDocs find
        } yield (bson get("article") toString, bson get("doc") toString)
        docs foreach (t => redis.set(t._1, t._2))
        
        //fill DB №1: Word => Set_of_articles
        redis select(1)
        val words = for {
            bson <- mongoWords find
        } yield (bson.get("word") toString, bson.get("docs").asInstanceOf[BasicDBList].toArray map {_ toString})
        words foreach {t => t._2 foreach (redis.sadd(t._1, _)) }
        
        //disconnect
        redis.disconnect()
        println("Done")
    }
}