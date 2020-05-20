package com.maxifom.pyenv_integration

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.File
import java.util.concurrent.TimeUnit

class InstallAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        var versions = mutableMapOf<String, Set<String>>()
        try {
//            TODO: select dir
            val pb = ProcessBuilder("/home/max/.pyenv/bin/pyenv", "install", "--list")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            pb.waitFor(5, TimeUnit.SECONDS)
            val versionsText = pb.inputStream.bufferedReader().readLines()
            for (v in versionsText.listIterator(1)) {
                val vTrim = v.trim()
                if (vTrim.contains("-dev")) {
                    continue
                }
                val splitted = vTrim.split("-")
                var name = "python.org"
                var version: String
                if (splitted.count() > 1) {
                    name = splitted[0]
                    version = splitted.subList(1, splitted.size).joinToString("-")
                } else {
                    version = splitted[0]
                }


                versions.putIfAbsent(name, setOf(version))
                versions[name] = versions[name]!! + version
            }
        } catch (e: Exception) {
            print("Exception listing pyenv versions: $e")
        }

        println(versions)

        var name = Messages.showEditableChooseDialog(
            "Choose python branch to install",
            "Choose python branch",
            Messages.getQuestionIcon(),
            versions.keys.toTypedArray(),
            versions.keys.first(),
            null
        )

        println("""Chose name $name""")

        val version = Messages.showEditableChooseDialog(
            "Choose $name Python version to install",
            "Install $name Python",
            Messages.getQuestionIcon(),
            versions[name]!!.toTypedArray(),
            versions[name]!!.first(),
            null
        )

        println("""Chose version $version""")

        if (name == "python.org") {
            name = ""
        } else {
            name += "-"
        }

        println("Chose python $name$version")

        val pb = ProcessBuilder("/home/max/.pyenv/bin/pyenv", "install", "$name$version")
            .redirectOutput(File("/dev/stdout"))
            .redirectError(File("/dev/stderr"))
            .start()
        pb.waitFor()
        println(pb.inputStream.bufferedReader().readText())
    }
}