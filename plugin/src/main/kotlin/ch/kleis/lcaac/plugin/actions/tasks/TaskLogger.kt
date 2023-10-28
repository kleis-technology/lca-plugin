package ch.kleis.lcaac.plugin.actions.tasks

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.terminal.TerminalExecutionConsole

interface TaskLogger {
    fun info(title: String, text: String)
    fun error(title: String, text: String)
}

class TerminalTaskLogger(private val term: TerminalExecutionConsole) : TaskLogger {
    override fun info(title: String, html: String) {
        val text = html.replace("<br>", "\n    ")
        term.print("$title: $text\n", ConsoleViewContentType.LOG_INFO_OUTPUT)
    }

    override fun error(title: String, text: String) {
        term.print("$title: $text\n", ConsoleViewContentType.LOG_ERROR_OUTPUT)
    }
}

class EventTaskLogger(private val project: Project) : TaskLogger {
    override fun info(title: String, text: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LcaAsCode")
            .createNotification(title, text, NotificationType.INFORMATION)
            .notify(project)
    }

    override fun error(title: String, text: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LcaAsCode")
            .createNotification(title, text, NotificationType.ERROR)
            .notify(project)
    }
}