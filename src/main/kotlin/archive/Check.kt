package archive

import Path
import MessageType
import util.*


fun checkArchiveVolume(filePaths: Array<Path>): Pair<MessageType,String> {
    var count = 0
    for( aPath in filePaths) {
        if (aPath.isSingleVolume() || aPath.isFirstVolume()) {
            count++
        }
    }

    return when (count) {
        1 -> Pair(MessageType.NoProblem, "Only one\nArchive")
        0 -> Pair(MessageType.Warning, "No\nArchive")
        else -> Pair(MessageType.Critical, "Too much\nArchives")
    }
}
