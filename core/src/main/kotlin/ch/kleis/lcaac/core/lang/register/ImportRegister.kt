package ch.kleis.lcaac.core.lang.register

import ch.kleis.lcaac.core.lang.expression.EImport

data class ImportKey(val name: String)

typealias ImportRegister<Q> = Register<ImportKey, EImport<Q>>
