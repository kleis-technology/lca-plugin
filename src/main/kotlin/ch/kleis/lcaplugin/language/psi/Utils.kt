package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/* This file contains functions that are used strictly more than once in two different places */

/**
 * Checks if a PsiElement is an LCA process.
 */
fun isProcess(element: PsiElement): Boolean =
        element.elementType == LcaTypes.rule(LcaLangParser.RULE_process) && element.parent is PsiProcess
