package ch.kleis.lcaplugin.language.psi.type.trait

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.factory.LcaFileFactory
import ch.kleis.lcaplugin.language.psi.factory.LcaUIDFactory
import ch.kleis.lcaplugin.language.psi.type.PsiUID
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiUIDOwner : PsiNameIdentifierOwner {
    fun getFullyQualifiedName(): String {
        val pkgName = (containingFile as LcaFile).getPackageName()
        return "$pkgName.${this.name}"
    }

    fun getUID(): PsiUID {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_uid))?.psi as PsiUID
    }

    override fun getName(): String {
        return (nameIdentifier as PsiUID).name
    }

    override fun setName(name: String): PsiElement {
        val uidNode: ASTNode? = node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_uid))
        if (uidNode != null) {
            val newIdentifier = LcaUIDFactory(
                LcaFileFactory(project)::createFile
            ).createUid(name)
            node.replaceChild(uidNode, newIdentifier.node)
        }
        return this
    }


    override fun getNameIdentifier(): PsiElement? {
        return getUID()
    }
}
