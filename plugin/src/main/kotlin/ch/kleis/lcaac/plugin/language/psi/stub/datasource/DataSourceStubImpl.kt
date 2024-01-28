package ch.kleis.lcaac.plugin.language.psi.stub.datasource

import ch.kleis.lcaac.plugin.psi.LcaDataSourceDefinition
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class DataSourceStubImpl(
    parent: StubElement<LcaDataSourceDefinition>,
    override val fqn: String,
) : StubBase<LcaDataSourceDefinition>(
    parent,
    LcaTypes.DATA_SOURCE_DEFINITION as IStubElementType<out StubElement<*>, *>
), DataSourceStub
