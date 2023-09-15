package ch.kleis.lcaac.plugin.language.psi.mixin.field

import ch.kleis.lcaac.plugin.language.psi.type.field.PsiSubstanceTypeField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiSubstanceTypeFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceTypeField
