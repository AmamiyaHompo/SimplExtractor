data class Leveled<T> (val level: Level, val datum: T)

enum class Level { SURE, SUSPICIOUS, IGNORE, NOTYET }

enum class MessageType { NoProblem, Warning, Bad, Critical }
