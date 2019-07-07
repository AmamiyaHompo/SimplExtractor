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


typealias ItemList = MutableMap<ItemID,Item>
typealias ResultRow = Array<String>
