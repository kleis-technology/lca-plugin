package ch.kleis.lcaplugin.language.psi.stub.substance

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class SubstanceStubImpl(
    parent: StubElement<PsiSubstance>,
    override val fqn: String
) :
    StubBase<PsiSubstance>(parent, LcaTypes.rule(LcaLangParser.RULE_substance) as IStubElementType<*, *>),
    SubstanceStub
