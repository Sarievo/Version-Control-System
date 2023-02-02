package svcs

import java.io.File
import java.time.LocalDateTime

class VersionControl {
    private val outDir = File("vcs")
    private val config = File("vcs/config.txt")
    private val index = File("vcs/index.txt")
    private val log = File("vcs/log.txt")

    init {
        if (!outDir.exists()) outDir.mkdir()
        else if (!outDir.isDirectory) throw Exception("'VCS' is a file")
    }

    fun help() {
        val help = """
        These are SVCS commands:
        config     Get and set a username.
        add        Add a file to the index.
        log        Show commit logs.
        commit     Save changes.
        checkout   Restore a file.
        """.trimIndent()
        println(help)
    }

    fun config(obj: String?) {
        if (obj == null) {
            if (config.exists() && config.isFile) {
//                https://hyperskill.org/learn/step/6351
                val lines = config.readText().trim()
                println("The username is $lines.")
            } else {
                println("Please, tell me who you are.")
            }
        } else {
//            https://hyperskill.org/learn/step/9710
            config.writeText(obj)
            println("The username is $obj.")
        }
    }

    fun add(obj: String?) {
        if (obj == null) {
            if (index.exists() && index.isFile) {
//                https://hyperskill.org/learn/step/6351
                println("Tracked files:")
                index.forEachLine { println(it.split(':')[0]) }
            } else {
                println("Add a file to the index.")
            }
        } else {
            val file = File(obj)
            if (file.exists()) {
                index.appendText("$obj:${obj.hashCode()}\n")
                println("The file '$obj' is tracked.")
            } else {
                println("Can't find '$obj'.")
            }
        }
    }

    fun log() {
        if (log.exists()) {
            log.forEachLine { println(it) }
        } else {
            println("No commits yet.")
        }
    }

    fun commit(obj: String?) {
        if (obj == null) {
            println("Message was not passed.")
        } else {
            if (!index.exists()) {
                println("Nothing to commit.")
            } else {
                val timestamp = LocalDateTime.now().toString()
                val hash = timestamp.hashCode()
                val validation = mutableMapOf<String, String>()
                var hasChange = false
                index.forEachLine {
                    val (path, fileHash) = it.split(':')
                    val newHash = File(path).readText().hashCode().toString()
                    if (newHash != fileHash) hasChange = true
                    validation[path] = newHash
                }

                if (hasChange) {
                    index.delete()
                    for ((k, v) in validation) {
                        index.appendText("$k:$v\n")
                        File(k).copyTo(File("vcs/commits/$hash/$k"))
                    }
                    val text = log.writeText(
                        "commit $hash\n" + "Author: ${config.readText()}\n"
                                + "$obj\n" + if (log.exists()) "\n${log.readText()}" else ""
                    )
                    println("Changes are committed.")
                } else {
                    println("Nothing to commit.")
                }
            }
        }
    }

    fun checkout(obj: String?) {
        if (obj == null) {
            println("Commit id was not passed.")
        } else {
            val file = File("vcs/commits/$obj")
            if (file.exists() && file.isDirectory) {
                file.listFiles()?.forEach {
                    it.copyTo(File(it.name), overwrite = true)
                }
                println("Switched to commit $obj.")
            } else {
                println("Commit does not exist.")
            }
        }
    }
}

fun main(args: Array<String>) {
    val i1 = if (args.isNotEmpty()) args[0] else null
    val i2 = if (args.size > 1) args[1] else null

    val control = VersionControl()

    when (i1) {
        null -> control.help()
        "--help" -> control.help()
        "config" -> control.config(i2)
        "add" -> control.add(i2)
        "log" -> control.log()
        "commit" -> control.commit(i2)
        "checkout" -> control.checkout(i2)
        else -> println("'$i1' is not a SVCS command.")
    }
}