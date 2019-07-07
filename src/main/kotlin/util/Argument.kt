package util

import com.xenomachina.argparser.ArgParser

class Config(parser: ArgParser) {
    val aIgnoringListPath by parser.storing("-I", help = "Path to ignoring list")
}
