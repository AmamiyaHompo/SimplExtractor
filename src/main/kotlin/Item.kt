import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem

import util.*


class Item (
    val dataCRC: Int?
    , val dataSize: DataSize
    , val path: Path
    , val idInArchive: ItemIndex
) {
    private val id: ItemID

    companion object {
        var serialCount = 1
    }

    init {
        id = serialCount
        serialCount += 1
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Item
        return dataCRC == that.dataCRC &&
                dataSize == that.dataSize &&
                path == that.path
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = hash * hashPrime + dataCRC.hashCode()
        hash = hash * hashPrime + dataSize.hashCode()
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
        , path = this.path
        , idInArchive = this.itemIndex
    )
}

typealias ItemIndex = Int
typealias ItemID = Int
typealias Name = String
typealias DataSize = Long
