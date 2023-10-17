package ch.kleis.lcaac.plugin.language.loader

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.RegisterException
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.plugin.language.psi.type.PsiProcess
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiIndicatorRef
import ch.kleis.lcaac.plugin.psi.*

class LcaMapper<Q>(
    private val ops: QuantityOperations<Q>
) {
    fun unitLiteral(lcaUnitDefinition: LcaUnitDefinition): DataExpression<Q> {
        return EUnitLiteral(
            UnitSymbol.of(lcaUnitDefinition.symbolField.getValue()),
            1.0,
            Dimension.of(lcaUnitDefinition.dimField!!.getValue())
        )
    }

    fun unitAlias(lcaUnitDefinition: LcaUnitDefinition): DataExpression<Q> {
        return EUnitAlias(
            lcaUnitDefinition.symbolField.getValue(),
            dataExpression(lcaUnitDefinition.aliasForField!!.dataExpression)
        )
    }

    fun process(
        lcaProcess: LcaProcess,
        globals: Register<DataExpression<Q>>,
    ): EProcessTemplate<Q> {
        val name = lcaProcess.name
        val labels = lcaProcess.getLabels().mapValues { EStringLiteral<Q>(it.value) }
        val locals = lcaProcess.getVariables().mapValues { dataExpression(it.value) }
        val guardedParams =
            lcaProcess.blockParametersList.flatMap(LcaBlockParameters::getGuardedAssignmentList)
                .associate { guardedAssignment ->
                    guardedAssignment.assignment.getDataRef().name to
                            (guardedAssignment.guard?.let {
                                EGuardedExpression(
                                    dataExpression(guardedAssignment.assignment.getValue()),
                                    dataExpression(it.low),
                                    dataExpression(it.high!!)
                                )
                            } ?: dataExpression(guardedAssignment.assignment.getValue()))
                }
        val symbolTable = SymbolTable(
            data = try {
                Register(globals.plus(guardedParams).plus(locals))
            } catch (e: RegisterException) {
                throw EvaluatorException("Conflict between local variable(s) ${e.duplicates} and a global definition.")
            },
        )
        val products = generateTechnoProductExchanges(lcaProcess, symbolTable)
        val inputs = lcaProcess.getInputs().map { technoInputExchange(it) }
        val emissions = lcaProcess.getEmissions().map { bioExchange(it, symbolTable) }
        val landUse = lcaProcess.getLandUse().map { bioExchange(it, symbolTable) }
        val resources = lcaProcess.getResources().map { bioExchange(it, symbolTable) }
        val biosphere = emissions.plus(resources).plus(landUse)
        val impacts = lcaProcess.getImpacts().map(::impact)
        val body = EProcess(
            name = name,
            labels = labels,
            products = products,
            inputs = inputs,
            biosphere = biosphere,
            impacts = impacts,
        )
        return EProcessTemplate(
            guardedParams,
            locals,
            body,
        )
    }

    private fun generateTechnoProductExchanges(
        psiProcess: PsiProcess,
        symbolTable: SymbolTable<Q>,
    ): List<ETechnoExchange<Q>> {
        return psiProcess.getProducts().map { technoProductExchange(it, symbolTable) }
    }

    fun substanceCharacterization(lcaSubstance: LcaSubstance): ESubstanceCharacterization<Q> {
        val substanceSpec = substanceSpec(lcaSubstance)
        val quantity = dataExpression(lcaSubstance.getReferenceUnitField().dataExpression)
        val referenceExchange = EBioExchange(quantity, substanceSpec)
        val impacts = lcaSubstance.getImpactExchanges().map { impact(it) }

        return ESubstanceCharacterization(
            referenceExchange,
            impacts,
        )
    }

    private fun substanceSpec(psiSubstance: LcaSubstance): ESubstanceSpec<Q> {
        return ESubstanceSpec(
            name = psiSubstance.getSubstanceRef().name,
            displayName = psiSubstance.getNameField().getValue(),
            type = SubstanceType.of(psiSubstance.getTypeField().getValue()),
            compartment = psiSubstance.getCompartmentField().getValue(),
            subCompartment = psiSubstance.getSubCompartmentField()?.getValue(),
            referenceUnit = EUnitOf(dataExpression(psiSubstance.getReferenceUnitField().dataExpression)),
        )
    }

    private fun substanceSpec(
        substanceSpec: LcaSubstanceSpec,
        quantity: DataExpression<Q>,
        symbolTable: SymbolTable<Q>
    ): ESubstanceSpec<Q> =
        ESubstanceSpec(
            name = substanceSpec.name,
            compartment = substanceSpec.getCompartmentField()?.getValue(),
            subCompartment = substanceSpec.getSubCompartmentField()?.getValue(),
            type = substanceSpec.getType(),
            referenceUnit = EUnitOf(EQuantityClosure(symbolTable, quantity)),
        )


    private fun impact(exchange: LcaImpactExchange): EImpact<Q> {
        return EImpact(
            dataExpression(exchange.dataExpression),
            indicatorSpec(exchange.indicatorRef),
        )
    }

    private fun indicatorSpec(variable: PsiIndicatorRef): EIndicatorSpec<Q> {
        return EIndicatorSpec(
            variable.name
        )
    }

    fun technoInputExchange(psiExchange: LcaTechnoInputExchange): ETechnoExchange<Q> {
        return ETechnoExchange(
            dataExpression(psiExchange.dataExpression),
            inputProductSpec(psiExchange.inputProductSpec),
        )
    }

    private fun outputProductSpec(
        outputProductSpec: LcaOutputProductSpec,
    ): EProductSpec<Q> {
        return EProductSpec(
            outputProductSpec.name
        )
    }

    private fun inputProductSpec(
        inputProductSpec: LcaInputProductSpec,
    ): EProductSpec<Q> {
        return EProductSpec(
            inputProductSpec.name,
            fromProcess = inputProductSpec.getProcessTemplateSpec()?.let { fromProcess(it) },
        )
    }

    private fun fromProcess(spec: LcaProcessTemplateSpec): FromProcess<Q> {
        val arguments = spec.argumentList
        val labelSelectors = spec.getMatchLabels()?.labelSelectorList ?: emptyList()
        return FromProcess(
            name = spec.name,
            matchLabels = MatchLabels(
                labelSelectors
                    .associate { selector -> selector.labelRef.name to dataExpression(selector.dataExpression) }
            ),
            arguments = arguments
                .associate { arg -> arg.parameterRef.name to dataExpression(arg.dataExpression) }
        )
    }

    private fun technoProductExchange(
        psiExchange: LcaTechnoProductExchange,
        symbolTable: SymbolTable<Q>,
    ): ETechnoExchange<Q> =
        ETechnoExchange(
            dataExpression(psiExchange.dataExpression),
            outputProductSpec(psiExchange.outputProductSpec)
                .copy(
                    referenceUnit = EUnitOf(
                        EQuantityClosure(
                            symbolTable,
                            dataExpression(psiExchange.dataExpression)
                        )
                    )
                ),
            psiExchange.outputProductSpec.allocateField?.let { allocation(it) }
        )

    private fun allocation(element: LcaAllocateField): DataExpression<Q> {
        return dataExpression(element.dataExpression)
    }

    private fun bioExchange(psiExchange: LcaBioExchange, symbolTable: SymbolTable<Q>): EBioExchange<Q> {
        val quantity = dataExpression(psiExchange.dataExpression)
        return EBioExchange(
            quantity,
            substanceSpec(psiExchange.substanceSpec, quantity, symbolTable)
        )
    }

    fun dataExpression(dataExpression: LcaDataExpression): DataExpression<Q> {
        fun getBinaryBranches(expr: LcaBinaryOperatorExpression) = Pair(
            dataExpression(expr.left),
            dataExpression(expr.right!!)
        )

        return when (dataExpression) {
            is LcaDataRef -> EDataRef(dataExpression.name)

            is LcaScaleQuantityExpression -> EQuantityScale(
                ops.pure(dataExpression.scale.text.toDouble()),
                dataExpression(dataExpression.dataExpression!!)
            )

            is LcaParenQuantityExpression -> dataExpression(dataExpression.dataExpression!!)

            is LcaExponentialQuantityExpression -> EQuantityPow(
                dataExpression(dataExpression.dataExpression),
                dataExpression.exponent.text.toDouble()
            )

            is LcaDivQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantityDiv(left, right)
            }

            is LcaMulQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantityMul(left, right)
            }

            is LcaAddQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantityAdd(left, right)
            }

            is LcaSubQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantitySub(left, right)
            }

            is LcaStringExpression -> EStringLiteral(dataExpression.text.trim('"'))
            else -> throw EvaluatorException("Unknown data expression: $dataExpression")
        }
    }
}
