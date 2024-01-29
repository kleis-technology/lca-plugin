package ch.kleis.lcaac.plugin.language.loader

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.*
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
        psiProcess: LcaProcess,
        globals: DataRegister<Q>,
        dataSources: DataSourceRegister<Q>,
    ): EProcessTemplate<Q> {
        val name = psiProcess.name
        val labels = psiProcess.getLabels().mapValues { EStringLiteral<Q>(it.value) }
        val locals = psiProcess.getVariables().mapValues { dataExpression(it.value) }
        val params = psiProcess.getParameters().mapValues { dataExpression(it.value) }
        val symbolTable = SymbolTable(
            data = try {
                Register(
                    globals
                        .plus(params.mapKeys { DataKey(it.key) })
                        .plus(locals.mapKeys { DataKey(it.key) })
                )
            } catch (e: RegisterException) {
                throw EvaluatorException("Conflict between local variable(s) ${e.duplicates} and a global definition.")
            },
            dataSources = dataSources
        )
        val products = generateTechnoProductExchanges(psiProcess, symbolTable)
        val inputs = psiProcess.getInputs().map { technoInputExchange(it) }
        val emissions = psiProcess.getEmissions().map { bioExchange(it, symbolTable) }
        val landUse = psiProcess.getLandUse().map { bioExchange(it, symbolTable) }
        val resources = psiProcess.getResources().map { bioExchange(it, symbolTable) }
        val biosphere = emissions.plus(resources).plus(landUse)
        val impacts = psiProcess.getImpacts().map(::impact)
        val body = EProcess(
            name = name,
            labels = labels,
            products = products,
            inputs = inputs,
            biosphere = biosphere,
            impacts = impacts,
        )
        return EProcessTemplate(
            params,
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


    private fun impact(ctx: LcaImpactExchange): ImpactBlock<Q> {
        return when {
            ctx.terminalImpactExchange != null -> {
                val indicatorRef = ctx.terminalImpactExchange!!.indicatorRef
                    ?: throw EvaluatorException("missing indicator")
                EImpactBlockEntry(
                    EImpact(
                        dataExpression(ctx.terminalImpactExchange!!.dataExpression),
                        indicatorSpec(indicatorRef),
                    )
                )
            }

            ctx.impactBlockForEach != null -> {
                val rowRef = ctx.impactBlockForEach!!.getDataRef().name
                val ctxDataSource = ctx.impactBlockForEach!!.getValue()
                    ?: throw EvaluatorException("missing data source")
                val dataSourceExpression = dataSourceExpression(ctxDataSource)
                val locals = ctx.impactBlockForEach!!.getVariablesList()
                    .flatMap { it.assignmentList }
                    .associate { it.getDataRef().name to dataExpression(it.getValue()) }
                val entries = ctx.impactBlockForEach!!.impactExchangeList
                    .map { impact(it) }
                EImpactBlockForEach(rowRef, dataSourceExpression, locals, entries)
            }

            else -> throw EvaluatorException("invalid impact exchange")
        }
    }

    private fun indicatorSpec(variable: PsiIndicatorRef): EIndicatorSpec<Q> {
        return EIndicatorSpec(
            variable.name
        )
    }

    fun technoInputExchange(ctx: LcaTechnoInputExchange): TechnoBlock<Q> {
        return when {
            ctx.terminalTechnoInputExchange != null -> {
                val inputProductSpec = ctx.terminalTechnoInputExchange!!.inputProductSpec
                    ?: throw EvaluatorException("missing substance spec")
                ETechnoBlockEntry(
                    ETechnoExchange(
                        dataExpression(ctx.terminalTechnoInputExchange!!.dataExpression),
                        inputProductSpec(inputProductSpec),
                    )
                )
            }

            ctx.technoBlockForEach != null -> {
                val rowRef = ctx.technoBlockForEach!!.getDataRef().name
                val ctxDataSource = ctx.technoBlockForEach!!.getValue()
                    ?: throw EvaluatorException("missing data source")
                val dataSourceExpression = dataSourceExpression(ctxDataSource)
                val locals = ctx.technoBlockForEach!!.getVariablesList()
                    .flatMap { it.assignmentList }
                    .associate { it.getDataRef().name to dataExpression(it.getValue()) }
                val entries = ctx.technoBlockForEach!!.technoInputExchangeList
                    .map { technoInputExchange(it) }
                ETechnoBlockForEach(rowRef, dataSourceExpression, locals, entries)
            }

            else -> throw EvaluatorException("invalid techno input exchange")
        }
    }

    private fun dataSourceExpression(ctx: LcaDataSourceExpression): DataSourceExpression<Q> {
        val ref = ctx.dataSourceRef.name
        val rowSelectors = ctx.rowFilter
            ?.rowSelectorList
            ?: emptyList()
        val filter = rowSelectors
            .associate {
                val dataExpression = it.dataExpression
                    ?: throw EvaluatorException("missing right-hand side")
                it.columnRef.name to dataExpression(dataExpression) }
        return if (filter.isEmpty()) EDataSourceRef(ref)
        else EFilter(EDataSourceRef(ref), filter)
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
                    .associate { selector ->
                        val dataExpression = selector.dataExpression
                            ?: throw EvaluatorException("missing right-hand side")
                        selector.labelRef.name to dataExpression(dataExpression) }
            ),
            arguments = arguments
                .associate { arg ->
                    val dataExpression = arg.dataExpression
                        ?: throw EvaluatorException("missing right-hand side")
                    arg.parameterRef.name to dataExpression(dataExpression)
                }
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

    private fun bioExchange(ctx: LcaBioExchange, symbolTable: SymbolTable<Q>): BioBlock<Q> {
        return when {
            ctx.terminalBioExchange != null -> {
                val quantity = dataExpression(ctx.terminalBioExchange!!.dataExpression)
                val substanceSpec = ctx.terminalBioExchange!!.substanceSpec
                    ?: throw EvaluatorException("missing substance spec")
                EBioBlockEntry(
                    EBioExchange(
                        quantity,
                        substanceSpec(substanceSpec, quantity, symbolTable)
                    )
                )
            }

            ctx.bioBlockForEach != null -> {
                val rowRef = ctx.bioBlockForEach!!.getDataRef().name
                val ctxDataSource = ctx.bioBlockForEach!!.getValue()
                    ?: throw EvaluatorException("missing data source")
                val dataSourceExpression = dataSourceExpression(ctxDataSource)
                val locals = ctx.bioBlockForEach!!.getVariablesList()
                    .flatMap { it.assignmentList }
                    .associate { it.getDataRef().name to dataExpression(it.getValue()) }
                val entries = ctx.bioBlockForEach!!.bioExchangeList
                    .map { bioExchange(it, symbolTable) }
                EBioBlockForEach(rowRef, dataSourceExpression, locals, entries)
            }

            else -> throw EvaluatorException("invalid bio exchange")
        }
    }

    fun dataSourceDefinition(ctx: LcaDataSourceDefinition): EDataSource<Q> {
        val location = ctx.locationFieldList.firstOrNull()?.value?.text?.trim('"')
            ?: throw EvaluatorException("missing location field")
        val schema = ctx.schemaDefinitionList.firstOrNull()
            ?.columnDefinitionList?.associate { it.getColumnRef().name to dataExpression(it.getValue()) }
            ?: throw EvaluatorException("missing schema")
        return EDataSource(
            location,
            schema,
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

            is LcaSliceExpression -> {
                val record = dataExpression(dataExpression.dataRef)
                val index = dataExpression.columnRef?.name
                    ?: throw EvaluatorException("missing column reference")
                ERecordEntry(record, index)
            }

            is LcaRecordExpression -> {
                val dataSource = dataSourceExpression(dataExpression.dataSourceExpression)
                return dataExpression.opDefaultRecord?.let { EDefaultRecordOf(dataSource) }
                    ?: dataExpression.opLookup?.let { EFirstRecordOf(dataSource) }
                    ?: throw EvaluatorException("Unknown record expression: $dataExpression ")
            }

            is LcaColExpression -> {
                val dataSourceExpression = dataExpression.dataSourceExpression
                    ?: throw EvaluatorException("missing data source reference")
                val dataSource = dataSourceExpression(dataSourceExpression)
                val columns = dataExpression.columnRefList
                    .map { it.name }
                ESumProduct(dataSource, columns)
            }

            else -> throw EvaluatorException("Unknown data expression: $dataExpression")
        }
    }
}
