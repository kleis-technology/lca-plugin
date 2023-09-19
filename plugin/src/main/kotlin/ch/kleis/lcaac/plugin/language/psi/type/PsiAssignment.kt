package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiAssignment : PsiNameIdentifierOwner {
    fun getDataRef(): PsiDataRef {
        return PsiTreeUtil.findChildrenOfType(this, LcaDataRef::class.java).elementAt(0)
    }

    fun getValue(): LcaDataExpression {
        return PsiTreeUtil.findChildrenOfType(this, LcaDataExpression::class.java).elementAt(1)
    }

    override fun getName(): String {
        return getDataRef().name
    }

    override fun setName(name: String): PsiElement {
        getDataRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getDataRef().nameIdentifier
    }
}
