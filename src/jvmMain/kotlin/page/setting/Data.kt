package page.setting

data class Billing(
    val access_until: Int,
    val account_name: String,
    val billing_address: BillingAddress,
    val billing_email: Any,
    val business_address: Any,
    val canceled: Boolean,
    val canceled_at: Any,
    val delinquent: Any,
    val hard_limit: Int,
    val hard_limit_usd: Double,
    val has_payment_method: Boolean,
    val `object`: String,
    val plan: Plan,
    val po_number: Any,
    val soft_limit: Int,
    val soft_limit_usd: Double,
    val system_hard_limit: Int,
    val system_hard_limit_usd: Double,
    val tax_ids: Any
)

data class BillingAddress(
    val city: String,
    val country: String,
    val line1: String,
    val line2: Any,
    val postal_code: String,
    val state: Any
)

data class Plan(
    val id: String,
    val title: String
)

data class Cost(
    val daily_costs: List<DailyCost>,
    val `object`: String,
    val total_usage: Double
)

data class DailyCost(
    val line_items: List<LineItem>,
    val timestamp: Double
)

data class LineItem(
    val cost: Double,
    val name: String
)