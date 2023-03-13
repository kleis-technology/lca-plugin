package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*

class RecursiveEvaluator(
    private val symbolTable: SymbolTable,
) {
    private val evaluator = Evaluator(symbolTable)

    fun eval(expression: TemplateExpression): SystemValue {
        val state = aux(State.empty(), expression)
        return state.asSystem()
    }

    private fun aux(
        state: State,
        expression: TemplateExpression,
        previousRequest: Request? = null
    ): State {
        // eval
        val p = evaluator.step(expression)
        val v = evaluator.asValue(p)
        if (v !is ProcessValue) {
            throw EvaluatorException("$v is not a process")
        }

        previousRequest?.let { request ->
            val provided = v.products.map { it.product.name }
            if (!provided.contains(request.product.name)) {
                throw EvaluatorException("${request.product.name} does not match any product of ${request.processRef}")
            }
        }

        // termination condition
        if (state.processes.contains(v)) {
            return state
        }

        // add evaluated process
        val newState = State(state)
        newState.processes.add(v)

        // add substance characterizations
        val everySubstance =
            TemplateExpression.eProcessFinal.expression.eProcess.biosphere compose
                    Every.list() compose
                    EBioExchange.substance.eSubstance
        everySubstance.getAll(p).forEach { substance ->
            symbolTable.getSubstanceCharacterization(substance.name)?.let {
                val scv = evaluator.eval(it)
                newState.substanceCharacterizations.add(scv)
            }
        }

        // recursively visit next process template instances
        val everyConstrainedProduct =
            TemplateExpression.eProcessFinal.expression.eProcess.inputs compose
                    Every.list() compose
                    ETechnoExchange.product.eConstrainedProduct

        for (it in everyConstrainedProduct.getAll(p)) {
            val product = it.product as EProduct
            val constraint = it.constraint
            if (constraint !is FromProcessRef) {
                continue
            }
            val processRef = constraint.template.name
            val template = symbolTable.getTemplate(processRef)
                ?: throw EvaluatorException("unbounded template reference $processRef")
            val arguments = constraint.arguments
            newState.add(
                aux(
                    newState,
                    EInstance(template, arguments),
                    Request(product, processRef),
                )
            )
        }
        return newState
    }

    class State(
        val processes: MutableSet<ProcessValue> = HashSet(),
        val substanceCharacterizations: MutableSet<SubstanceCharacterizationValue> = HashSet(),
    ) {
        constructor(state: State) : this(
            HashSet(state.processes),
            HashSet(state.substanceCharacterizations),
        )

        companion object {
            fun empty(): State {
                return State()
            }
        }

        fun add(state: State) {
            processes.addAll(state.processes)
            substanceCharacterizations.addAll(state.substanceCharacterizations)
        }

        fun asSystem(): SystemValue {
            return SystemValue(
                processes = processes.toList(),
                substanceCharacterizations = substanceCharacterizations.toList(),
            )
        }
    }

    data class Request(
        val product: EProduct,
        val processRef: String,
    )
}

