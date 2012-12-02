package ru.tomtrix.dm.crawler

/**
 * @author tom-trix
 *
 */
object SearchConsole {
    def main(args: Array[String]): Unit =
        while (true) {
            //suggest user to choose the mode 
            println(">> select a search mode\n     1 = FIND_ALL_WORDS_AND_ONLY_THEM\n     2 = FIND_ALL_WORDS\n     3 = FIND_ANY_WORD\n     4 = FIND_BY_KEYWORDS\n     5 = FIND_BY_DATE")
            readLine match {
                case "1" => Search.searchByText("1")
                case "2" => Search.searchByText("2")
                case "3" => Search.searchByText("3")
                case "4" => Search.searchByText("3", true)
                case "5" => Search.searchByDate
            }
        }
}