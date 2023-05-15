package ch.kleis.lcaplugin.language.psi.mixin.quantity

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityDivideTerm
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityDivideTermMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityDivideTerm
