package ru.tomtrix.dm.crawler

import ru.tomtrix.dm.crawler.Common._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.BasicDBList
import redis.clients.jedis.Jedis
import collection.JavaConverters._

/**
 * @author tom-trix
 *
 */
object RedisSearch {

    def main(args: Array[String]): Unit = {
        //get list of words
        val words = readLine.split(splitRegex).toList map { _.toLowerCase } filter (t => !t.trim.isEmpty && !stopWords.contains(t)) map { stem(_) }
        
        //query to MongoDB
        val occurencesList = for {
            word <- words
            bson <- mongoWords.find(MongoDBObject("word" -> word))
            occurences <- try { Some(bson.get("docs").asInstanceOf[BasicDBList].toArray.toList.map { _.asInstanceOf[Int] }) }
        } yield occurences
        val mongoList = if (occurencesList.size > 0 && occurencesList.size == words.size)
            occurencesList.foldLeft(occurencesList(0)) { (a, b) => a intersect b }
        else List.empty
        mongoList foreach println
        println("=========")
        
        //query to Redis
        val redis = new Jedis("localhost")
        redis select(1)
        
        val t = redis.sinter(words:_*)
        t.toArray() foreach println
    }

}