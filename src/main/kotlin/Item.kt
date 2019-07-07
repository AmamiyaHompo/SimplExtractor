import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem

import util.*


class Item (
    val dataCRC: Int
    , val dataSize: DataSize
    , val modifiedDate: Date
    , val path: Path
    , val idInArchive: ItemIndex
) {
    val id: ItemID

    companion object {
        var serialCount = 1
    }

    init {
        id = serialCount
        serialCount += 1
    }

    fun getFullName() = path.getFullName()


    private fun checkArchiveName(fullName: String): Boolean? =
        when {
            fullName.getExtension() == "exe" -> null // Make more logic
            fullName.isArchive() -> true
            else -> false
        }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Item
        return dataCRC == that.dataCRC &&
                dataSize == that.dataSize &&
                modifiedDate == that.modifiedDate &&
                path == that.path
    }

    fun equalsWithoutPath(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Item
        return dataCRC == that.dataCRC &&
                dataSize == that.dataSize &&
                modifiedDate == that.modifiedDate &&
                path.last() == that.path.last()
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = hash * hashPrime + dataCRC.hashCode()
        hash = hash * hashPrime + dataSize.hashCode()
        hash = hash * hashPrime + modifiedDate.hashCode()
        hash = hash * hashPrime + path.hashCode()
        hash = hash * hashPrime + idInArchive.hashCode()
        hash = hash * hashPrime + id.hashCode()
        return hash
    }
}

fun ISimpleInArchiveItem.makeItemFromArchiveItem(): Item {
    return Item (
        dataCRC = this.crc
        , dataSize = this.size
        , modifiedDate = this.lastWriteTime.time
        , path = this.path
        , idInArchive = this.itemIndex
    )
}

typealias ItemIndex = Int
typealias ItemID = Int
typealias Date = Long
typealias Name = String
typealias DataSize = Long
