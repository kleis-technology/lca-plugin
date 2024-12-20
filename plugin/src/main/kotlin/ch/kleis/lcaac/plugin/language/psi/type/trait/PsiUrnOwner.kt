package ch.kleis.lcaac.plugin.language.psi.type.trait

import ch.kleis.lcaac.plugin.LcaFileType
import ch.kleis.lcaac.plugin.language.psi.type.PsiUrn
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiUrnOwner : PsiNameIdentifierOwner {
    fun getUrn(): PsiUrn

    override fun getNameIdentifier(): PsiElement? {
        return getUrn()
    }

    override fun getName(): String {
        return getUrn().getParts().joinToString(".")
    }

    override fun setName(name: String): PsiElement {
        val urnElement: ASTNode? = node.findChildByType(LcaTypes.URN)
        if (urnElement != null) {
            val newUrnElement = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    "_Dummy_.${LcaFileType.INSTANCE.defaultExtension}",
                    LcaFileType.INSTANCE,
                    name
                )
            node.replaceChild(urnElement, newUrnElement.node)
        }
        return this
    }
}
