package ch.kleis.lcaac.plugin.language.ide.syntax

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile


class MyDslTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (c == '@') {
            AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null)
            return Result.STOP
        }
        return Result.CONTINUE
    }
}