package ru.tomtrix.dm.crawler

import redis.clients.jedis.Jedis
import ru.tomtrix.dm.crawler.Common._
import com.mongodb.BasicDBList

object Redis {

    def fillRedis = {
        //connect
        val redis = new Jedis("localhost")
        
        //заполняем 0-ю БД записями: №_статьи => статья
        val docs = for {
            bson <- mongoDocs find
        } yield (bson get("article") toString, bson get("doc") toString)
        docs foreach (t => redis.set(t._1, t._2))
        
        //заполняем 1-ю БД записями: word => мн-во_статей
        redis select(1)
        val words = for {
            bson <- mongoWords find
        } yield (bson.get("word") toString, bson.get("docs").asInstanceOf[BasicDBList].toArray map {_ toString})
        words foreach {t => t._2 foreach (redis.sadd(t._1, _)) }
        
        //disconnect
        redis.disconnect()
    }
    
    def main(args: Array[String]): Unit = {
        //fillRedis
        val redis = new Jedis("localhost")
        redis select(1)
        println(redis.sismember("поедут", "370111"));
        println("Done")
    }
}