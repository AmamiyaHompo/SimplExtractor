package util

import java.io.File

import directoryDelimiter
import dateFormat
import Path



fun generateStringFromFileList (strings : List<File>): String {
    val internalString = strings.map{it.toString().getFullName()}.joinToString(separator = "\n")
    return arrayOf("<\n", internalString, "\n>").joinToString(separator = "")
}

fun getFirstOrSingleArchivePaths(paths: Array<Path>) : Array<Path> {
    var firstOrSingle: MutableList<String> = mutableListOf()
    for ( aPath in paths ) {
        if ( aPath.isArchive() ) {
            // I knew this can be replaced by single if by using `maybePartNumber`
            // But I want to leave this structure for easy reading
            if (aPath.isSingleVolume()) {
                firstOrSingle.add(aPath)
            } else if (aPath.isFirstVolume()) {
                firstOrSingle.add(aPath)
            }
        }
    }
    return firstOrSingle.toTypedArray()
}

fun filePathAnalyze(files: List<File>): Array<Path> {
    val pathArray = files.map{it.toString()}.toTypedArray()

    return getFirstOrSingleArchivePaths(pathArray)
}

fun String.getFullName(): String =
    this.substringAfterLast(directoryDelimiter)

fun String.getFileName(): String =
    this.substringAfterLast(directoryDelimiter).substringBeforeLast(".")

fun String.getExtension(): String =
    this.substringAfterLast(directoryDelimiter).substringAfterLast(".","")

fun String.getDirectory(): String =
    this.substringBeforeLast(directoryDelimiter,"")

fun String.isArchive(): Boolean {
    val archiveExts: Array<String> = arrayOf("rar", "zip", "7z", "exe")
    for ( aExt in archiveExts ) {
        if ( this.getExtension() == aExt ) {
            return true
        }
    }
    return false
}

fun String.isArchiveSensitively(): Boolean? {
    val archiveExts: Array<String> = arrayOf("rar", "zip", "7z")
    for ( aExt in archiveExts ) {
        if ( this.getExtension() == aExt ) {
            return true
        }
    }
    if (this.getExtension() == "exe") return null
    return false
}

fun String.isEXE() = this.getExtension() == "exe"

/*
  * null -> SingleVolume
  * 1 -> First Volume
  * otherwise -> Not single nor first volume
 */
fun String.maybePartNumber(): Int? {
    val maybeNumberString = this.substringAfterLast(".part","")
    //println(String.format("<maybePartNumber>: %s",maybeNumberString))
    return maybeNumberString.toIntOrNull()
}

fun String.isSingleVolume(): Boolean = getFileName().maybePartNumber() == null

fun String.isFirstVolume(): Boolean = getFileName().maybePartNumber() == 1

fun String.getCommonNameOfMultiVolume(): String = getFileName().substringBeforeLast(".part")

fun Long.dateFormatter(): String {
    return dateFormat.format(java.util.Date(this))
}

fun MutableList<String>.getSame(): Pair<Boolean,List<Pair<String,String>>> {
    val pairList = mutableListOf<Pair<String,String>>()
    for (str in this)
        // TODO: Want to do more efficiently, but I couldn't find something like `splitAt` or `breakOnEnd` in Haskell
        pairList.add(Pair(str.getDirectory(),str.getFullName()))
    val head = pairList[0].second
    for (strPair in pairList.drop(1)) {
        if (head != strPair.second) return Pair(false, pairList)
    }
    return Pair(true, pairList)
}
