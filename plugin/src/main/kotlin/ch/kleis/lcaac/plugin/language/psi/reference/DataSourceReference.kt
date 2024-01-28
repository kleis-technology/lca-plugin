package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.datasource.DataSourceStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataSourceRef
import ch.kleis.lcaac.plugin.psi.LcaDataSourceDefinition
import com.intellij.psi.stubs.StubIndex

class DataSourceReference(
    element: PsiDataSourceRef
) : GlobalUIDOwnerReference<PsiDataSourceRef, LcaDataSourceDefinition>(
    element,
    { project, fqn -> DataSourceStubKeyIndex.findDataSources(project, fqn) },
    { project -> StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.DATA_SOURCES, project) }
)
