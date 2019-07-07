import java.util.*
import java.io.File

import archive.*
import util.*


class TheSet constructor (anArchiveSet: Array<Path>, defaultOutputDirectory: Path) {
    val theItemList: ItemList = mutableMapOf()
    val tableInstance: Int

    companion object {
        var tableInstanceSerial = 1
    }

    init {
        tableInstance = tableInstanceSerial
        tableInstanceSerial++

    }

    private fun registerArchiveSetItemsRecord() {
    }



    fun generateResultStringList(): List<ResultRow> {
        val aResult = mutableListOf<ResultRow>()
        for ( aItemEntry in theItemList) {
            // TODO: Not yet implemented
        }
        return aResult.toList()
    }
}

data class ItemKey (
    val isArchive: Boolean?
    , val dataCRC: Int
    , val dataSize: DataSize
    , val dupCount: Int
) : Comparable<ItemKey> {
    companion object {
        val comparatorKey =
            compareByDescending(ItemKey::dataSize)
                .thenBy(ItemKey::dataCRC)
                .thenBy(ItemKey::dupCount)
    }
    override fun compareTo(other: ItemKey): Int =
        if (this.isArchive == other.isArchive) {
            comparatorKey.compare(this,other)
        }
        else when (this.isArchive) {
            true -> -1
            false -> 1
            null -> if (other.isArchive!!) 1 else -1
        }
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(if (isArchive==null) "? " else if (isArchive) "A " else "F ")
        stringBuilder.append(String.format("%08X", this.dataCRC))
        stringBuilder.append("  ")
        stringBuilder.append(String.format("%10d", this.dataSize))
        stringBuilder.append("  ")
        stringBuilder.append(String.format("%2d", this.dupCount))
        stringBuilder.append("  ")
        return stringBuilder.toString()
    }
}


typealias ItemList = MutableMap<ItemID,Item>
typealias ResultRow = Array<String>
