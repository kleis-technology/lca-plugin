package ch.kleis.lcaac.plugin.ide.imports

import ch.kleis.lcaac.plugin.MyBundle
import ch.kleis.lcaac.plugin.ide.component.ProgressBar
import ch.kleis.lcaac.plugin.ide.imports.progressbar.AsynchronousImportWorker
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapperPeer
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Pair
import com.intellij.openapi.wm.impl.welcomeScreen.ActionGroupPanelWrapper
import com.intellij.openapi.wm.impl.welcomeScreen.FlatWelcomeFrame
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.update.UiNotifyConnector
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists


class LcaImportDialog<P>(private val panel: P, title: String, project: Project?) :
    DialogWrapper(project) where P : ImportHandler, P : JPanel {

    private var panelAndActions: Pair<JPanel?, JBList<AnAction?>> = Pair(null, JBList())

    private var worker: AsynchronousImportWorker? = null

    companion object {
        fun validateRegularFile(value: String, component: JComponent): ValidationInfo? {
            val libPath = Path.of(value)
            return if (libPath.notExists() || !libPath.isRegularFile()) {
                ValidationInfo(MyBundle.message("lca.dialog.import.library.file.error"), component)
            } else {
                null
            }
        }

        fun validatePackageIsValid(value: String, component: JComponent): ValidationInfo? {
            return if (!Regex("[a-zA-Z0-9]*").matches(value)
                || Regex("^[0-9]").matches(value)
            ) {
                ValidationInfo(MyBundle.message("lca.dialog.import.package.error"), component)
            } else {
                null
            }
        }

        fun validateNonEmpty(value: String, component: JComponent): ValidationInfo? {
            return if (value.isBlank()) {
                ValidationInfo(MyBundle.message("lca.dialog.import.field.mandatory.error"), component)
            } else {
                null
            }
        }
    }

    init {
        super.init()
        this.title = title
        val peer: DialogWrapperPeer = peer
        val pane = peer.rootPane
        if (pane != null) {
            val size = JBUI.size(FlatWelcomeFrame.MAX_DEFAULT_WIDTH, FlatWelcomeFrame.DEFAULT_HEIGHT)
            pane.minimumSize = size
            pane.preferredSize = size
        }
    }

    override fun createCenterPanel(): JComponent? {
        val action = object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {// Nothing
            }
        }
        val root = DefaultActionGroup(action)

        Disposer.register(disposable) { root.removeAll() }
        val groupActions = ActionGroupPanelWrapper.createActionGroupPanel(root, null, disposable)
        val component = groupActions.first
        component.add(panel)
        panelAndActions = groupActions
        UiNotifyConnector.doWhenFirstShown(panelAndActions.second) {
            ScrollingUtil.ensureSelectionExists(
                panelAndActions.second
            )
        }
        ActionGroupPanelWrapper.installQuickSearch(groupActions.second)
        return component
    }


    override fun getPreferredFocusedComponent(): JComponent {
        return FlatWelcomeFrame.getPreferredFocusedComponent(panelAndActions)
    }

    private fun importOnSuccess() {
        close(OK_EXIT_CODE)
    }

    private fun importOnError(progressBar: ProgressBar) {
        panel.remove(progressBar)
        panel.repaint()
        getButton(myOKAction)?.isEnabled = true
        getButton(myCancelAction)?.isEnabled = true
    }

    override fun doOKAction() {
        if (okAction.isEnabled) {

            val progressBar = ProgressBar()
            val importer = panel.importer()
            val worker =
                AsynchronousImportWorker(importer, this::importOnSuccess, { importOnError(progressBar) }, progressBar)
            getButton(myOKAction)?.isEnabled = false
            getButton(myCancelAction)?.isEnabled = false
            panel.add(progressBar)
            pack()
            worker.start()
        }
    }

    override fun doCancelAction() {
        super.doCancelAction()
        worker?.active = false
    }

    public override fun doValidate(): ValidationInfo? {
        return panel.doValidate()
    }


    override fun getStyle(): DialogStyle {
        return DialogStyle.COMPACT
    }


    override fun createSouthPanel(): JComponent {
        val result = super.createSouthPanel()
        helpAction.isEnabled = true
        return result
    }

    override fun getHelpId(): String {
        return "not null"
    }

    override fun doHelpAction() {
        if (myHelpAction.isEnabled) {
            val helpUrl = MyBundle.message("lca.dialog.import.help.url")
            BrowserUtil.browse(helpUrl)
        }
    }
}

