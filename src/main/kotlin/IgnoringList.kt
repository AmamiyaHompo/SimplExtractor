import util.*


class IgnoringItem(
    private val itemCRC: Leveled<Int>,
    private val itemSize: Leveled<DataSize>,
    private val itemName: Leveled<Name>
) {
    fun match(item: Item): Boolean {
        var result = true
        var checked = false
        if (itemCRC.level == Level.SURE) {
            checked = true
            result = result && (itemCRC.datum == item.dataCRC)
        }
        if (itemSize.level == Level.SURE) {
            checked = true
            result = result && (itemSize.datum == item.dataSize)
        }
        if (itemName.level == Level.SURE) {
            checked = true
            result = result && (itemName.datum == item.path.getFullName())
        }
        return if (checked) result else false
    }
}

class IgnoringList (
    private val ignoringList: List<IgnoringItem>
) {
    fun match(item: Item): Boolean {
        for ( ignoringItem in ignoringList) {
            if ( ignoringItem.match(item)) {
                return true
            }
        }
        return false
    }
}

fun ignoringListFromString(content: List<String>): IgnoringList {
    val rawIgnoringList = mutableListOf<IgnoringItem>()
    for (line in content) {
        val tokens = line.split("|")

        val itemCRCL = Level.valueOf(tokens[0].trim())
        val itemCRCV = tokens[1].toLong(16).toInt()
        val itemSIZEL = Level.valueOf(tokens[2].trim())
        val itemSIZEV = tokens[3].trim().toLong()
        val itemNameL = Level.valueOf(tokens[6].trim())
        val itemNameV = tokens[7]
        rawIgnoringList.add(IgnoringItem(
            Leveled(itemCRCL,itemCRCV)
            , Leveled(itemSIZEL,itemSIZEV)
            , Leveled(itemNameL, itemNameV)
        ))
    }

    return IgnoringList(rawIgnoringList.toList())
}

fun readIgnoringList(inputPath: Path): IgnoringList {
    return ignoringListFromString(readFileAsLinesUsingBufferedReader(inputPath))
}
