package ch.kleis.lcaac.plugin.language.psi.stub.datasource

import ch.kleis.lcaac.plugin.psi.LcaDataSourceDefinition
import com.intellij.psi.stubs.StubElement

interface DataSourceStub : StubElement<LcaDataSourceDefinition> {
    val fqn: String
}
