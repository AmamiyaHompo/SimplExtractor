package util

/*
Copy from https://github.com/eugenp/tutorials/tree/master/core-kotlin/src/main/kotlin/com/baeldung/filesystem
 */

import Message
import java.io.File

import Path
import MessageType

fun readFileAsLinesUsingBufferedReader(fileName: String): List<String> = File(fileName).bufferedReader().readLines()


fun checkArchiveExistence(filePaths: Array<Path>): Message {
    for (aPath in filePaths) {
        if (!File(aPath).exists())
            return Pair(MessageType.Bad,"Can't access\n$aPath")
        else
            println("Exist: $aPath")
    }
    return Pair(MessageType.NoProblem, "\n")
}
