package archive

import Message
import util.*

import Path
import MessageType


fun checkArchiveVolume(filePaths: Array<Path>): Message {
    var count = 0
    for( aPath in filePaths) {
        if (aPath.isSingleVolume() || aPath.isFirstVolume()) {
            count++
        }
    }

    return when (count) {
        1 -> Pair(MessageType.NoProblem, "Only one\nArchive")
        0 -> Pair(MessageType.Critical, "No\nArchive")
        else -> Pair(MessageType.NoProblem, "Many\nArchives")
    }
}
