package ru.tomtrix.dm.crawler

import com.mongodb.BasicDBList
import ru.tomtrix.dm.crawler.Common._
import com.mongodb.casbah.commons.MongoDBObject

/** Search engine
 * @author tom-trix
 */
object Search {
    def main(args: Array[String]): Unit = {
        while (true) {
            //suggest user to choose the mode 
            println(">> select a search mode\n     1 = FIND_ALL_WORDS_AND_ONLY_THEM\n     2 = FIND_ALL_WORDS\n     3 = FIND_ANY_WORD")
            var mode = readLine
            if (mode == "1" || mode == "2" || mode == "3") {

                //get list of searching words
                print("input searching string: ")
                val words = readLine.split(splitRegex).toList map { _.toLowerCase } filter (t => !t.trim.isEmpty && !stopWords.contains(t)) map { stem(_) }
                println(">> trying to find: " + words);

                //query to the Reverse Index
                val occurencesList = for {
                    word <- words
                    bson <- mongoWords.find(MongoDBObject("word" -> word))
                    occurences <- try { Some(bson.get("docs").asInstanceOf[BasicDBList].toArray.toList map { _.asInstanceOf[Int] }) }
                } yield {
                    println(""">> word """" + word + """" found in: """ + occurences);
                    occurences
                }
                println(">> found words: " + occurencesList.size + "/" + words.size)

                //get the final list depending on should it be united or intersected from the others
                val finalList = if (occurencesList.size > 0) mode match {
                    case "1" =>
                        if (occurencesList.size == words.size)
                            occurencesList.foldLeft(occurencesList(0)) { (a, b) => a intersect b }
                        else List.empty
                    case "2" => occurencesList.foldLeft(occurencesList(0)) { (a, b) => a intersect b }
                    case "3" => occurencesList.foldLeft(occurencesList(0)) { (a, b) => a ++ b } distinct
                    case _ => List.empty
                }
                else List.empty
                println(">> final list (after " + (if (mode == "FIND_ANY_WORD") "uniting" else "intersecting") + "): " + finalList)

                //get the found documents from MongoDB
                val docs = for {
                    cursor <- finalList map { t => mongoDocs find (MongoDBObject("article" -> t)) }
                    bson <- cursor
                } yield bson.get("doc").asInstanceOf[BasicDBList].toArray.toList
                println("\n==== results ====\n")
                docs foreach { t => t foreach println; println("\n=================\n") }
                println(">> total documents found: " + finalList.size + "\n\n\n")
            }
        }
    }

}