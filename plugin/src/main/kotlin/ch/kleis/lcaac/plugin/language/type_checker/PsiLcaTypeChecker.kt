package ch.kleis.lcaac.plugin.language.type_checker

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.type.*
import ch.kleis.lcaac.plugin.language.psi.type.PsiBlockForEach
import ch.kleis.lcaac.plugin.language.psi.type.PsiProcess
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.language.psi.type.unit.UnitDefinitionType
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.psi.PsiElement

class PsiLcaTypeChecker {
    private val rec = RecursiveGuard()

    fun check(element: PsiElement): Type {
        return when (element) {
            is LcaUnitDefinition -> checkUnit(element)
            is LcaDataExpression -> checkDataExpression(element)
            is LcaGlobalAssignment -> checkDataExpression(element.getValue())
            is LcaAssignment -> checkDataExpression(element.getValue())
            is LcaLabelAssignment -> TString
            is LcaTerminalTechnoInputExchange -> checkTerminalTechnoInputExchange(element)
            is LcaTechnoProductExchange -> checkTechnoProductExchange(element)
            is LcaTerminalBioExchange -> checkTerminalBioExchange(element)

            // the following checks the type of the data source expression in the block for each
            is PsiBlockForEach -> {
                val columns = columnsOf(element.getValue().dataSourceRef)
                return TRecord(columns.mapValues { checkDataExpression(it.value) })
            }

            else -> throw PsiTypeCheckException("Uncheckable type: $element")
        }
    }

    private fun checkTerminalBioExchange(lcaBioExchange: LcaTerminalBioExchange): TBioExchange {
        return rec.guard { el: LcaTerminalBioExchange ->
            val tyQuantity = checkDataExpression(el.dataExpression, TQuantity::class.java)
            val substanceSpec = el.substanceSpec
                ?: throw EvaluatorException("missing substance spec")
            val name = substanceSpec.name
            val comp = substanceSpec.getCompartmentField()?.getValue() ?: ""
            val subComp = substanceSpec.getSubCompartmentField()?.getValue()
            substanceSpec.reference?.resolve()?.let {
                if (it is LcaSubstance) {
                    val tyRefQuantity =
                        checkDataExpression(it.getReferenceUnitField().dataExpression, TQuantity::class.java)
                    if (tyRefQuantity.dimension != tyQuantity.dimension) {
                        throw PsiTypeCheckException(
                            "Incompatible dimensions: expecting ${tyRefQuantity.dimension}, found ${tyQuantity.dimension}"
                        )
                    }
                } else {
                    throw PsiTypeCheckException("Expected a PsiSubstance element but was ${it::class}")
                }
            }
            TBioExchange(TSubstance(name, tyQuantity.dimension, comp, subComp))
        }(lcaBioExchange)
    }

    private fun checkProcessArguments(element: PsiProcess): Map<String, TypeDataExpression> {
        return rec.guard { el: PsiProcess ->
            el.getParameters().mapValues { checkDataExpression(it.value) }
        }(element)
    }

    private fun checkTerminalTechnoInputExchange(element: LcaTerminalTechnoInputExchange): TTechnoExchange {
        return rec.guard { el: LcaTerminalTechnoInputExchange ->
            val tyQuantity = checkDataExpression(el.dataExpression, TQuantity::class.java)
            val inputProductSpec = el.inputProductSpec
                ?: throw EvaluatorException("missing input product spec")
            val productName = inputProductSpec.name
            inputProductSpec.reference?.resolve()?.let {
                val outputProductSpec = it as LcaOutputProductSpec
                val tyOutputExchange = check(outputProductSpec.getContainingTechnoExchange())
                if (tyOutputExchange !is TTechnoExchange) {
                    throw PsiTypeCheckException("expected TTechnoExchange, found $tyOutputExchange")
                }
                if (tyOutputExchange.product.dimension != tyQuantity.dimension) {
                    throw PsiTypeCheckException("incompatible dimensions: ${tyQuantity.dimension} vs ${tyOutputExchange.product.dimension}")
                }

                val psiProcess = outputProductSpec.getContainingProcess()
                val tyArguments = checkProcessArguments(psiProcess)
                inputProductSpec.getProcessTemplateSpec()
                    ?.argumentList
                    ?.forEach { arg ->
                        val key = arg.parameterRef.name
                        val value = arg.dataExpression
                        val tyActual = checkDataExpression(value)
                        val tyExpected = tyArguments[key] ?: throw PsiTypeCheckException("unknown parameter $key")
                        if (tyExpected != tyActual) {
                            throw PsiTypeCheckException("incompatible types: expecting $tyExpected, found $tyActual")
                        }
                    }
            }
            inputProductSpec.getProcessTemplateSpec()
                ?.getMatchLabels()
                ?.labelSelectorList
                ?.forEach { label ->
                    val tyActual = checkDataExpression(label.dataExpression)
                    val tyExpected = TString
                    if (tyExpected != tyActual) {
                        throw PsiTypeCheckException("incompatible types: expecting $tyExpected, found $tyActual")
                    }
                }
            TTechnoExchange(TProduct(productName, tyQuantity.dimension))
        }(element)
    }


    private fun checkTechnoProductExchange(element: LcaTechnoProductExchange): TTechnoExchange {
        return rec.guard { el: LcaTechnoProductExchange ->
            val tyQuantity = checkDataExpression(el.dataExpression, TQuantity::class.java)
            val productName = el.outputProductSpec.name
            TTechnoExchange(TProduct(productName, tyQuantity.dimension))
        }(element)
    }

    private fun checkUnit(element: LcaUnitDefinition): TUnit {
        return when (element.getType()) {
            UnitDefinitionType.LITERAL -> checkUnitLiteral(element)
            UnitDefinitionType.ALIAS -> checkUnitAlias(element)
        }
    }

    private fun checkUnitAlias(element: LcaUnitDefinition): TUnit {
        return rec.guard { el: LcaUnitDefinition ->
            val tyQuantity = checkDataExpression(el.aliasForField!!.dataExpression, TQuantity::class.java)
            TUnit(tyQuantity.dimension)
        }(element)
    }

    private fun checkUnitLiteral(psiUnitDefinition: LcaUnitDefinition): TUnit {
        return TUnit(Dimension.of(psiUnitDefinition.dimField!!.getValue()))
    }

    private fun <T> checkDataExpression(element: LcaDataExpression, cls: Class<T>): T {
        val ty = checkDataExpression(element)
        if (ty.javaClass != cls) {
            throw PsiTypeCheckException("expected ${cls.simpleName}, found $ty")
        }
        @Suppress("UNCHECKED_CAST")
        return ty as T
    }

    private fun checkDataExpression(element: LcaDataExpression): TypeDataExpression {
        return when (element) {
            is PsiDataRef -> checkDataRef(element)
            is LcaStringExpression -> TString
            is LcaScaleQuantityExpression -> element.dataExpression?.let { checkDataExpression(it) }
                ?: throw PsiTypeCheckException("missing expression")

            is LcaParenQuantityExpression -> element.dataExpression?.let { checkDataExpression(it) }
                ?: throw PsiTypeCheckException("missing expression")

            is LcaExponentialQuantityExpression -> {
                val exponent = element.exponent.text.toDouble()
                val tyBase = checkDataExpression(element.dataExpression, TQuantity::class.java)
                TQuantity(tyBase.dimension.pow(exponent))
            }

            is LcaBinaryOperatorExpression -> {
                val tyLeft = checkDataExpression(element.left, TQuantity::class.java)
                val tyRight = checkDataExpression(element.right!!, TQuantity::class.java)
                when (element) {
                    is LcaAddQuantityExpression, is LcaSubQuantityExpression -> {
                        if (tyLeft.dimension == tyRight.dimension) {
                            tyLeft
                        } else {
                            throw PsiTypeCheckException("incompatible dimensions: ${tyLeft.dimension} vs ${tyRight.dimension}")
                        }
                    }

                    is LcaMulQuantityExpression -> TQuantity(tyLeft.dimension.multiply(tyRight.dimension))
                    is LcaDivQuantityExpression -> TQuantity(tyLeft.dimension.divide(tyRight.dimension))
                    else -> throw PsiTypeCheckException("Unknown binary expression $element")
                }
            }

            is LcaColExpression -> {
                val columns = columnsOf(element.dataSourceExpression.dataSourceRef)
                val requestedColumns = element.columnRefList
                    .map { it.name }
                val unknownColumns = requestedColumns
                    .filter { !columns.containsKey(it) }
                if (unknownColumns.isNotEmpty())
                    throw PsiTypeCheckException("columns $unknownColumns not found in schema of '${element.dataSourceExpression.dataSourceRef.name}'")
                return columns
                    .filterKeys { requestedColumns.contains(it) }
                    .values
                    .map { checkDataExpression(it, TQuantity::class.java) }
                    .reduce { acc, e -> TQuantity(acc.dimension.multiply(e.dimension)) }
            }

            is LcaRecordExpression -> {
                val columns = columnsOf(element.dataSourceExpression.dataSourceRef)
                return TRecord(columns.mapValues { checkDataExpression(it.value) })
            }

            is LcaSliceExpression -> {
                val columnDefinition = element.columnRef.reference.resolve() as LcaColumnDefinition?
                    ?: throw PsiTypeCheckException("unknown column '${element.columnRef.name}'")
                checkDataExpression(columnDefinition.getValue())
            }
            else -> throw PsiTypeCheckException("Unknown expression $element")
        }
    }

    private fun columnsOf(e: LcaDataSourceRef): Map<String, LcaDataExpression> {
        val ds = e.reference.resolve() as LcaDataSourceDefinition?
            ?: throw PsiTypeCheckException("unknown data source '${e.name}'")
        val schema = ds.schemaDefinitionList
            .firstOrNull()
            ?: throw PsiTypeCheckException("missing schema in '${ds.name}'")
        return schema.columnDefinitionList
            .associate { it.name to it.dataExpression }
    }

    private fun checkDataRef(element: PsiDataRef): TypeDataExpression {
        return rec.guard { el: PsiDataRef ->
            el.reference.resolve()
                ?.let {
                    when (val ty = check(it)) {
                        is TQuantity -> ty
                        is TUnit -> TQuantity(ty.dimension)
                        is TString -> ty
                        is TRecord -> ty
                        else -> throw PsiTypeCheckException("expected TQuantity, TUnit, TString or TRecord, found $ty")
                    }
                }
                ?: throw PsiTypeCheckException("unbound reference ${el.name}")
        }(element)
    }
}
