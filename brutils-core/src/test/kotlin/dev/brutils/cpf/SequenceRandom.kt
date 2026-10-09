package dev.brutils.cpf

import kotlin.random.Random

/** A [Random] whose `nextInt(until)` returns [values] in order, counting the calls. */
internal class SequenceRandom(
    values: List<Int>,
) : Random() {
    private val remaining = values.iterator()

    var calls: Int = 0
        private set

    override fun nextInt(until: Int): Int {
        calls += 1
        val value = remaining.next()
        require(value in 0 until until) { "$value is out of range for nextInt($until)" }
        return value
    }

    override fun nextBits(bitCount: Int): Int = throw UnsupportedOperationException("Only nextInt(until) is scripted.")
}
