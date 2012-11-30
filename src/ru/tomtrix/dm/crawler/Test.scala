package ru.tomtrix.dm.crawler

import ru.tomtrix.dm.crawler.Common._
import com.mongodb.casbah.commons.MongoDBObject
import java.util.Date
import java.text.spi.DateFormatProvider
import java.beans.SimpleBeanInfo

object Test {

    def main(args: Array[String]): Unit = {
        mongoWords.remove(MongoDBObject.empty)
        val s = "23 сентября 2006 г.".split(" ")
        mongoWords.insert(MongoDBObject("d" -> new Date(s(2).toInt - 1900, months indexOf stem(s(1)), s(0) toInt)))
        println("Done");
    }

}