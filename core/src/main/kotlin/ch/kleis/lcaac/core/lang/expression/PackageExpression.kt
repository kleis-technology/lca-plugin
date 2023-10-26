package ch.kleis.lcaac.core.lang.expression

import ch.kleis.lcaac.core.lang.register.*

sealed interface PackageExpression<Q>

data class EPackage<Q>(
    val name: String,
    val data: DataRegister<Q> = DataRegister.empty(),
    val dimensions: DimensionRegister = DimensionRegister.empty(),
    val processTemplates: ProcessTemplateRegister<Q> = ProcessTemplateRegister.empty(),
    val substanceCharacterizations: SubstanceCharacterizationRegister<Q> = SubstanceCharacterizationRegister.empty(),
    val imports: ImportRegister<Q> = ImportRegister.empty(),
) : PackageExpression<Q>

data class EImport<Q>(
    val name: String,
    val arguments: Map<String, DataExpression<Q>> = emptyMap(),
    val with: Map<String, EProductSpec<Q>> = emptyMap(),
) : PackageExpression<Q>

data class EImportAlias<Q>(
    val alias: String,
) : PackageExpression<Q>
