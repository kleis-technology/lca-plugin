package ch.kleis.lcaac.core.lang.register

import ch.kleis.lcaac.core.lang.expression.PackageExpression

data class ImportKey(val name: String)

typealias ImportRegister<Q> = Register<ImportKey, PackageExpression<Q>>
