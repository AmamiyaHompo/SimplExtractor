package util

import com.xenomachina.argparser.ArgParser

class Config(parser: ArgParser) {
    val aWorkingDirectory by parser.storing("-W", help = "Directory for extracting archives")
    val aTargetDirectory by parser.storing("-T", help = "Directory to move files")
    val aIgnoringListPath by parser.storing("-I", help = "Path to ignoring list")
}
