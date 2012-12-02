package ru.tomtrix.dm.crawler

import java.util.Date
import com.mongodb.casbah.Imports._
import ru.tomtrix.dm.crawler.Common._
import java.util.regex.Pattern

/**
 * Search engine
 * @author tom-trix
 */
object Search {
    def searchByText(mode: String) = {
        //get it
        print("input searching string: ")
        val s = readLine
        //get list of searching words
        val words = s.split(splitRegex).toList map { _.toLowerCase } filter (t => !t.trim.isEmpty && !stopWords.contains(t)) map { stem(_) }
        //get list of boolean values (true, if searched, false, if excluded)
        val incls = s.split(splitRegexWithoutExc).toList filter (t => !t.trim.isEmpty && !stopWords.contains(t)) map { !_.startsWith("!") }
        println(">> trying to find: " + words);
        println(">> included in search: " + incls);

        //query to the Reverse Index (obtain map: is_included -> occurences)
        val occurencesList = for {
            word <- words zip incls
            bson <- mongoWords.find(MongoDBObject("word" -> word._1))
            occurences <- try { Some(word._2 -> bson.get("docs").asInstanceOf[BasicDBList].toArray.toList.map { _.asInstanceOf[Int] }) }
        } yield {
            println(""">> word """" + word._1 + """" found in: """ + occurences._2);
            occurences
        }
        println(">> found words: " + occurencesList.size + "/" + words.size)

        //get the final list depending on should it be united or intersected
        val finalList = if (occurencesList.size > 0) mode match {
            case "1" =>
                if (occurencesList.size == words.size)
                    occurencesList.foldLeft(occurencesList(0)._2) { (a, b) => if (b._1) a intersect b._2 else a -- b._2 }
                else List.empty
            case "2" => occurencesList.foldLeft(occurencesList(0)._2) { (a, b) => if (b._1) a intersect b._2 else a -- b._2 }
            case "3" => occurencesList.foldLeft(occurencesList(0)._2) { (a, b) => if (b._1) a ++ b._2 else a -- b._2 } distinct
            case _ => List.empty
        }
        else List.empty
        println(">> final list (after " + (if (mode == "3") "uniting" else "intersecting") + "): " + finalList)

        //get the found documents from MongoDB
        val result = for {
            cursor <- finalList map { t => mongoDocs find (MongoDBObject("article" -> t)) }
            bson <- cursor
        } yield bson.get("doc").asInstanceOf[BasicDBList].toArray.toList ++ Seq(bson.get("date").asInstanceOf[Date].toGMTString.substring(0, 11))

        //print
        println("\n==== results ====\n")
        result foreach { t => t foreach println; println("\n=================\n") }
        println(">> total documents found: " + finalList.size + "\n\n\n")
    }

    def searchByDate = {
        //get it
        println("""input date query (e.g.: ">= 26 октября" or "between 4 июля and 6 июля" ): """)
        var result: Iterator[List[Object]] = null
        val s = readLine.trim.toLowerCase
        
        //try to handle BETWEEN-query
        var m = Pattern.compile("between (.*) (.*) and (.*) (.*)").matcher(s)
        if (m.matches() && m.lookingAt()) {
            val d1 = new Date(new Date().getYear(), months.indexOf((stem(m.group(2)))), m.group(1).toInt+1)
            val d2 = new Date(new Date().getYear(), months.indexOf((stem(m.group(4)))), m.group(3).toInt+1)
            result = for {
                bson <- mongoDocs.find("date" $gte d1 $lte d2)
            } yield bson.get("doc").asInstanceOf[BasicDBList].toArray.toList ++ Seq(bson.get("date").asInstanceOf[Date].toGMTString.substring(0, 11))
        }
        
        //try to handle usual queries
        else {
            m = Pattern.compile(".* (.*) (.*)").matcher(s)
            if (m.matches() && m.lookingAt()) {
                val d = new Date(new Date().getYear(), months.indexOf((stem(m.group(2)))), m.group(1).toInt+1)
                result = for {
                    bson <- s.substring(0, 2) match {
                        case "< " => mongoDocs.find("date" $lt d)
                        case "<=" => mongoDocs.find("date" $lte d)
                        case "> " => mongoDocs.find("date" $gt d)
                        case ">=" => mongoDocs.find("date" $gte d)
                        case "= " => mongoDocs.find(MongoDBObject("date" -> d))
                        case _ => mongoDocs.find(MongoDBObject("_id" -> 0))
                    }
                } yield bson.get("doc").asInstanceOf[BasicDBList].toArray.toList ++ Seq(bson.get("date").asInstanceOf[Date].toGMTString.substring(0, 11))
            }
        }
        
        //print
        println("\n==== results ====\n")
        result foreach { t => t foreach println; println("\n=================\n") }
    }
}