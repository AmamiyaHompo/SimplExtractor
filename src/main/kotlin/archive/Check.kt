package archive

import Path
import MessageType


fun checkArchiveVolume(packagedFilePaths: Array<Path>): Pair<MessageType,String> {
    // TODO: Implement checking missing volume
    return Pair(MessageType.NoProblem, "No Problem\nwith Archive Volume")
}