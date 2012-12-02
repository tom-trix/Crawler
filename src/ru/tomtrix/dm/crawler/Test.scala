package ru.tomtrix.dm.crawler

import ru.tomtrix.dm.crawler.Common._
import com.mongodb.casbah.commons.MongoDBObject
import java.util.Date
import java.text.spi.DateFormatProvider
import java.beans.SimpleBeanInfo

object Test {

    def main(args: Array[String]): Unit = {
        val a = List(1, 2, 3, 4)
        val b = List(5, 6, 3, 4)
        val c = b -- a
        c foreach println
        
    }

}