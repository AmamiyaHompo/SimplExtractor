import archive.*
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem
import util.*
import java.io.File

class Archive (
    private val ans: ArchiveAndStream
    , val archivePath: Path
) {
    private val itemList: MutableList<Item> = mutableListOf()

    init {
        val simpleArchive = ans.inArchive.simpleInterface
        for (sItem in simpleArchive.archiveItems) {
            if (!sItem.isFolder)
                addNewItem(sItem)
        }
    }

    fun addNewItem(sItem: ISimpleInArchiveItem) {
        val anItem = sItem.makeItemFromArchiveItem()
        if (theIgnoringList.match(anItem)) {
            print("Skip: ${anItem.path}\n")
        } else {
            itemList.add(anItem)
        }
    }

    fun extractAll(): Pair<MessageType,String> {
        val idList: MutableList<Int> = mutableListOf()
        for ( anItem in itemList ) {
            idList.add(anItem.idInArchive)
        }

        try {
            Extract(archivePath, theWorkingDirectory, false, null)
                //.extractSomething(getInArchive(), idList.toIntArray())
                .extractEverything()
        } catch (e: Exception) {
            return Pair(MessageType.Critical, "Fail when extracting $archivePath")
        }
        return Pair(MessageType.NoProblem, "No Problem")
    }

    fun renameAll(): Pair<MessageType,String> {
        val renamePath = archivePath.getFullName().replaceSpace()
        for (anItem in itemList) {
            val oldFile = File(theWorkingDirectory+directoryDelimiter+anItem.path)
            val newFile = File(theWorkingDirectory
                    +directoryDelimiter
                    +anItem.path.getFileName()
                    +"."
                    +renamePath
                    +"."
                    +anItem.path.getExtension()
            )
            if (oldFile.renameTo(newFile)) {
                println("Success to rename ${oldFile.absolutePath}")
            }
            else {
                println("Fail to rename ${oldFile.absolutePath} to ${newFile.absolutePath}")
                return Pair(MessageType.Critical,"Fail to rename ${oldFile.absolutePath} to ${newFile.absolutePath}")
            }
        }
        return Pair(MessageType.NoProblem,"No Problem")
    }

    fun getInArchive(): IInArchive = ans.inArchive
    fun closeArchive() = ans.close()
}

typealias ItemMap = MutableList<Item>
