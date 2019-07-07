data class Leveled<T> (val level: Level, val datum: T)

enum class Level { SURE, SUSPICIOUS, IGNORE, NOTYET }

fun Level.toShortString() = when (this) {
    Level.SURE -> "SR"
    Level.SUSPICIOUS -> "SS"
    Level.IGNORE -> "IG"
    Level.NOTYET -> "NY"
}

enum class MessageType { NoProblem, Warning, Bad, Critical }

enum class ResultType { All, Same, Diff, Ignored }
