package ch.kleis.lcaac.plugin.ide.imports

import ch.kleis.lcaac.plugin.imports.Importer
import com.intellij.openapi.ui.ValidationInfo

interface ImportHandler {

    fun importer(): Importer
    fun doValidate(): ValidationInfo?
}
