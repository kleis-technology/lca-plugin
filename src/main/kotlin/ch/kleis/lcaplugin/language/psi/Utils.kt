package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/* This file contains functions that are used strictly more than once in two different places */

/**
 * Checks if a PsiElement is an LCA process.
 */
fun isProcess(element: PsiElement): Boolean =
        element.elementType == LcaTokenTypes.PROCESS_KEYWORD && element.parent is PsiProcess
