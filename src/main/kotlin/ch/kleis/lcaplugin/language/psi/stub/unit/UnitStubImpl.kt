package ch.kleis.lcaplugin.language.psi.stub.unit

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class UnitStubImpl(
    parent: StubElement<PsiUnitDefinition>,
    override val fqn: String,
) : StubBase<PsiUnitDefinition>(
    parent,
    LcaTypes.rule(LcaLangParser.RULE_unitDefinition) as IStubElementType<out StubElement<*>, *>
), UnitStub
