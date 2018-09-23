package ro.ande.dekont.vo

import java.math.BigDecimal
import java.util.*

data class Sum(
        val amount: BigDecimal,
        val currency: Currency
)