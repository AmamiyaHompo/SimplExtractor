package util

/*
Copy from https://github.com/eugenp/tutorials/tree/master/core-kotlin/src/main/kotlin/com/baeldung/filesystem
 */

import Message
import java.io.File

import Path
import MessageType


fun readFileLineByLineUsingForEachLine(fileName: String) = File(fileName).forEachLine { println(it) }

fun readFileAsLinesUsingUseLines(fileName: String): List<String> = File(fileName)
    .useLines { it.toList() }

fun readFileAsLinesUsingBufferedReader(fileName: String): List<String> = File(fileName).bufferedReader().readLines()

fun readFileAsLinesUsingReadLines(fileName: String): List<String> = File(fileName).readLines()

fun readFileAsTextUsingInputStream(fileName: String) =
    File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)

fun readFileDirectlyAsText(fileName: String): String = File(fileName).readText(Charsets.UTF_8)


fun writeFileUsingPrintWriter(fileName: String, fileContent: String) =
    File(fileName).printWriter().use { out -> out.print(fileContent) }

fun writeFileUsingBufferedWriter(fileName: String, fileContent: String) =
    File(fileName).bufferedWriter().use { out -> out.write(fileContent) }

fun writeFileDirectly(fileName: String, fileContent: String) =
    File(fileName).writeText(fileContent)

fun writeFileDirectlyAsBytes(fileName: String, fileContent: String) =
    File(fileName).writeBytes(fileContent.toByteArray())


fun checkArchiveExistence(filePaths: Array<Path>): Message {
    for (aPath in filePaths) {
        if (!File(aPath).exists())
            return Pair(MessageType.Bad,"Can't access\n$aPath")
        else
            println("Exist: $aPath")
    }
    return Pair(MessageType.NoProblem, "\n")
}
