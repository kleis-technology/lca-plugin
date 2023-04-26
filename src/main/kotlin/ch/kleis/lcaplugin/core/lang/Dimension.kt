package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.value.UnitValue
import kotlin.math.round

class InvalidPowerException : Throwable("", null, false, false)

class Dimension(elements: Map<String, Double>) {
    private val elements: Map<String, Double>

    override fun toString(): String {
        return elements.entries.joinToString(".") {
            simpleDimToString(it)
        }
    }

    private fun simpleDimToString(basic: Map.Entry<String, Double>): String {
        return if (basic.value == 1.0) {
            basic.key
        } else {
            val power =
                if (basic.value == round(basic.value)) {
                    toPower(String.format("%d", basic.value.toLong())) ?: "^[${basic.value}]"
                } else {
                    "^[${basic.value}]"
                }
            "${basic.key}$power"
        }
    }

    private fun toPower(f: String): String? {
        // Parallel, no assumption on order, Iterable<A>.reduce only requires A to be a Semigroup
        // However, kotlin has no mapReduce, so we loose some optimization by evaluating eagerly the map even once a
        // null is found.
        val pureKotlin = f.map{ convert(it)}.reduce{ mAcc, mS ->
            mAcc?.let { acc ->
                mS?.let { s ->
                    acc.plus(s) }
            }
        }

        // Using a left fold and that String is a Monoid (with "" as the empty element).
        // Short-circuits the evaluation of the transformation, guarantees order but cannot parallelize.
        // Note that here some smart compilers paired with lazy collections would short-circuit the rest of the fold
        // on first null.
        val otherPureKotlin = f.fold("") { acc: String?, c: Char ->
            acc?.let {
                convert(c)?.let { superC ->
                    it.plus(superC)
                }
            }
        }

        // Magic! If only we had proper types... Arrow 1.2.0 ?
        val withFunctionalStuff: String? = f.foldMap{ convert(it) }

        return pureKotlin
    }

    /* Functional approach:
     * - see https://stackoverflow.com/questions/3242361/what-is-called-and-what-does-it-do
     * - see https://hackage.haskell.org/package/base-4.18.0.0/docs/Prelude.html#v:foldMap
     * - see https://en.wikipedia.org/wiki/Absorbing_element
     *
     * (note that I tried implementing this demo using interfaces and classes to mimic the
     * traits (scala) / class (haskell) concept, but type parameters in kotlin are too weak.
     *
     * We consider Monoids with an absorbing elements and show that String? is one:
     * class Semigroup a => MonoidWithAbsorbing a where
     *  mempty :: a is the empty element
     *  mabsorbing :: a is the absorbing element
     *  mappend :: a -> a -> a (also written <>)
     *
     * and forall x :: a, mempty <> x = x <> mempty = x
     *     forall x :: a, mabsorbing <> x = x <> mabsorbing = mabsorbing
     *     forall x, y, z :: a, x <> (y <> z) = (x <> y) <> z
     *
     */
    private fun mStringMempty(): String? = ""
    private fun mStringMabsorbing(): String? = null
    private fun mStringMappend(mA: String?, mB: String?): String? =
            mA?.let { a -> mB?.let { b -> a.plus(b) }}

    /* Secondly, we consider the class Foldable t where
     *  foldMap :: Monoid m => t a -> (a -> m) -> m
     * and show that we have Foldable String (here implemented with String? as monoid,
     * because type generics in kotlin are *horrible*):
     */
    private fun String.foldMap(operation: (Char) -> String?): String? =
        this.fold(mStringMempty()) { acc, elem -> mStringMappend(acc, operation(elem)) }

    /* End functional demo */

    private fun convert(c: Char): String? {
        val result: Int? = when (c) {
            '0' -> 0x2070
            '1' -> 0x00B9
            '2' -> 0x00B2
            '3' -> 0x00B3
            in '4'..'9' -> 0x2070 + (c.code - 48)
            '.' -> 0x02D9
            '-' -> 0x207B
            else -> null
        }
        return result?.let { Character.toString(it) }
    }


    init {
        this.elements = elements.filter { it.value != 0.0 }
    }

    companion object {
        val None = Dimension(emptyMap())
        fun of(name: String): Dimension {
            return if (name == "none") None else Dimension(mapOf(Pair(name, 1.0)))
        }

        fun of(name: String, power: Int): Dimension {
            return if (name == "none") None else Dimension(mapOf(Pair(name, power.toDouble())))
        }
    }

    fun getDefaultUnitValue(): UnitValue {
        return UnitValue("default($this)", 1.0, this)
    }

    fun isNone(): Boolean {
        return this.elements.isEmpty()
    }

    fun multiply(other: Dimension): Dimension {
        val es = HashMap<String, Double>(elements)
        other.elements.entries.forEach { entry ->
            es[entry.key] = es[entry.key]?.let { it + entry.value }
                ?: entry.value
        }
        return Dimension(es)
    }

    fun divide(other: Dimension): Dimension {
        val es = HashMap<String, Double>(elements)
        other.elements.entries.forEach { entry ->
            es[entry.key] = es[entry.key]?.let { it - entry.value }
                ?: (-entry.value)
        }
        return Dimension(es)
    }

    fun pow(n: Double): Dimension {
        val es = HashMap<String, Double>()
        elements.entries.forEach { entry ->
            es[entry.key] = n * entry.value
        }
        return Dimension(es)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dimension

        return elements != other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }


}
