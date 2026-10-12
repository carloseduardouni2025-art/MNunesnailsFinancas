package br.com.mnunesnails.financas.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test fun acceptsBrazilianAndDecimalNotation() {
        assertEquals(4590L, parseCents("45,90"))
        assertEquals(4590L, parseCents("45.90"))
        assertEquals(4500L, parseCents(" 45 "))
        assertEquals(1L, parseCents("0,01"))
    }
    @Test fun rejectsInvalidAmountsWithoutRounding() {
        listOf("", "0", "-1", "45,999", "NaN", "1e3", "1.000,00", "92233720368547758.08")
            .forEach { assertNull("Invalid amount: $it", parseCents(it)) }
    }
}
