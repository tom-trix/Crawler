package ru.tomtrix.dm.crawler.redis

import ru.tomtrix.dm.crawler.Common._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.BasicDBList
import redis.clients.jedis.Jedis
import scala.compat.Platform

/**
 * @author tom-trix
 * This object is to compare the searching capabilities of MongoDB and Redis
 */
object RedisSearch {

    def main(args: Array[String]): Unit = {
        val redis = new Jedis("localhost")
        redis select (1)
        while (true) {
            //ask for search string & get a list of words
            print("input searching string: ")
            val words = readLine.split(splitRegex).toList map { _.toLowerCase } filter (t => !t.trim.isEmpty && !stopWords.contains(t)) map { stem(_) }

            //query to MongoDB
            var time = Platform currentTime
            val occurencesList = for {
                word <- words
                bson <- mongoWords.find(MongoDBObject("word" -> word))
                occurences <- try { Some(bson.get("docs").asInstanceOf[BasicDBList].toArray.toList.map { _.asInstanceOf[Int] }) }
            } yield occurences
            val mongoList = if (occurencesList.size > 0 && occurencesList.size == words.size)
                occurencesList.foldLeft(occurencesList(0)) { (a, b) => a intersect b }
            else List.empty
            time = Platform.currentTime - time
            println("==== M O N G O ====")
            mongoList foreach println
            println("time = " + time + " msec")

            //query to Redis
            time = Platform currentTime
            val redisList = redis sinter (words: _*)
            time = Platform.currentTime - time
            println("==== R E D I S ====")
            redisList.toArray foreach println
            println("time = " + time + " msec")
        }
    }
}