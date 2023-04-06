package ch.kleis.lcaplugin.language.type_checker

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.type.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.quantity.*
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.language.psi.type.unit.UnitDefinitionType
import com.intellij.psi.PsiElement

class PsiLcaTypeChecker {
    fun check(element: PsiElement): Type {
        return when (element) {
            is PsiUnitDefinition -> checkUnit(element)
            is PsiQuantity -> checkQuantity(element)
            is PsiGlobalAssignment -> checkQuantity(element.getValue())
            is PsiAssignment -> checkQuantity(element.getValue())
            is PsiTechnoInputExchange -> checkTechnoInputExchange(element)
            is PsiTechnoProductExchange -> checkTechnoProductExchange(element)
            else -> throw IllegalArgumentException()
        }
    }

    private fun checkProcessArguments(element: PsiProcess): Map<String, TQuantity> {
        return element.getParameters().mapValues { checkQuantity(it.value) }
    }

    private fun checkTechnoInputExchange(element: PsiTechnoInputExchange): TTechnoExchange {
        val tyQuantity = checkQuantity(element.getQuantity())
        val productName = element.getProductRef().name
        element.getProductRef().reference.resolve()?.let {
            val tyProductExchange = check(it)
            if (tyProductExchange !is TTechnoExchange) {
                throw PsiTypeCheckException("expected TTechnoExchange, found $tyProductExchange")
            }
            if (tyProductExchange.product.dimension != tyQuantity.dimension) {
                throw PsiTypeCheckException("incompatible dimensions: ${tyQuantity.dimension} vs ${tyProductExchange.product.dimension}")
            }
        }
        element.getFromProcessConstraint()?.let {
            val psiProcess = it.getProcessTemplateRef().reference.resolve() as PsiProcess?
                ?: throw PsiTypeCheckException("unbound reference ${it.getProcessTemplateRef().name}")
            val tyArguments = checkProcessArguments(psiProcess)
            it.getArguments()
                .forEach { (key, value) ->
                    val tyActual = checkQuantity(value)
                    val tyExpected = tyArguments[key] ?: throw PsiTypeCheckException("unknown parameter $key")
                    if (tyExpected != tyActual) {
                        throw PsiTypeCheckException("incompatible dimensions: expecting ${tyExpected.dimension}, found ${tyActual.dimension}")
                    }
                }
        }
        return TTechnoExchange(TProduct(productName, tyQuantity.dimension))
    }


    private fun checkTechnoProductExchange(element: PsiTechnoProductExchange): TTechnoExchange {
        val tyQuantity = checkQuantity(element.getQuantity())
        val productName = element.getProductRef().name
        return TTechnoExchange(TProduct(productName, tyQuantity.dimension))
    }

    private fun checkUnit(psiUnitDefinition: PsiUnitDefinition): TUnit {
        return when (psiUnitDefinition.getType()) {
            UnitDefinitionType.LITERAL -> checkUnitLiteral(psiUnitDefinition)
            UnitDefinitionType.ALIAS -> checkUnitAlias(psiUnitDefinition)
        }
    }

    private fun checkUnitAlias(psiUnitDefinition: PsiUnitDefinition): TUnit {
        val tyQuantity = checkQuantity(psiUnitDefinition.getAliasForField().getValue())
        return TUnit(tyQuantity.dimension)
    }

    private fun checkUnitLiteral(psiUnitDefinition: PsiUnitDefinition): TUnit {
        return TUnit(Dimension.of(psiUnitDefinition.getDimensionField().getValue()))
    }

    private fun checkQuantity(psiQuantity: PsiQuantity): TQuantity {
        val tyLeft = checkQuantityTerm(psiQuantity.getTerm())
        return when (psiQuantity.getOperationType()) {
            AdditiveOperationType.ADD, AdditiveOperationType.SUB -> {
                val tyRight = checkQuantity(psiQuantity.getNext()!!)
                if (tyLeft.dimension != tyRight.dimension) {
                    throw PsiTypeCheckException("incompatible dimensions: ${tyLeft.dimension} vs ${tyRight.dimension}")
                }
                return tyLeft
            }

            null -> tyLeft
        }
    }

    private fun checkQuantityTerm(term: PsiQuantityTerm): TQuantity {
        val tyLeft = checkQuantityFactor(term.getFactor())
        return when (term.getOperationType()) {
            MultiplicativeOperationType.MUL -> {
                val tyRight = checkQuantityTerm(term.getNext()!!)
                return TQuantity(tyLeft.dimension.multiply(tyRight.dimension))

            }

            MultiplicativeOperationType.DIV -> {
                val tyRight = checkQuantityTerm(term.getNext()!!)
                return TQuantity(tyLeft.dimension.divide(tyRight.dimension))

            }

            null -> tyLeft
        }
    }

    private fun checkQuantityFactor(factor: PsiQuantityFactor): TQuantity {
        val tyPrimitive = checkQuantityPrimitive(factor.getPrimitive())
        return factor
            .getExponent()
            ?.let { TQuantity(tyPrimitive.dimension.pow(it)) }
            ?: tyPrimitive
    }

    private fun checkDimensionOf(quantityRef: PsiQuantityRef): Dimension {
        return quantityRef.reference.resolve()
            ?.let {
                when (val ty = check(it)) {
                    is TQuantity -> ty.dimension
                    is TUnit -> ty.dimension
                    else -> throw PsiTypeCheckException("expected TQuantity or TUnit, found $ty")
                }
            }
            ?: Prelude.unitMap[quantityRef.name]?.dimension
            ?: throw PsiTypeCheckException("unbound reference ${quantityRef.name}")
    }

    private fun checkQuantityPrimitive(primitive: PsiQuantityPrimitive): TQuantity {
        return when (primitive.getType()) {
            QuantityPrimitiveType.LITERAL -> {
                val dim = checkDimensionOf(primitive.getRef())
                TQuantity(dim)
            }

            QuantityPrimitiveType.QUANTITY_REF -> {
                val dim = checkDimensionOf(primitive.getRef())
                TQuantity(dim)
            }

            QuantityPrimitiveType.PAREN -> checkQuantity(primitive.getQuantityInParen())
        }
    }
}
