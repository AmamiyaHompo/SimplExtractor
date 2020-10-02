import javafx.application.Application
import com.xenomachina.argparser.ArgParser

import archive.*
import util.*

fun main(args : Array<String>) {
    println("SimplExtract-Kotlin")

    if (!jBindingChecker()) error("Fail to initialize 7Zip-JBinding")

    ArgParser(args).parseInto(::Config).run {
        try {
            initialize(aIgnoringListPath)
        } catch (e: Exception) {
            println("Fail to load IgnoringList")
            return
        }
        theWorkingDirectory = aWorkingDirectory
        theTargetDirectory = aTargetDirectory
        Application.launch(EntryPoint().javaClass, *args)
    }

}

fun initialize(ignoringListConfigPath: Path) {
    // Set IgnoringList
    theIgnoringList = readIgnoringList(ignoringListConfigPath)
}
