package br.com.mnunesnails.financas.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.UUID

data class Service(val id: String, val name: String, val cents: Long, val description: String = "", val duration: String = "", val category: String = "")
data class Income(val service: String, val cents: Long, val date: String, val payment: String = "", val notes: String = "")

class FinanceRepository(context: Context) {
    private val preferences = context.getSharedPreferences("finance", Context.MODE_PRIVATE)
    fun services(): List<Service> = read("services").let { array ->
        List(array.length()) { index -> array.getJSONObject(index).let {
            Service(it.getString("id"), it.getString("name"), it.getLong("cents"), it.optString("description"), it.optString("duration"), it.optString("category"))
        } }
    }
    fun incomes(): List<Income> = read("incomes").let { array ->
        List(array.length()) { index -> array.getJSONObject(index).let {
            Income(it.getString("service"), it.getLong("cents"), it.getString("date"), it.optString("payment"), it.optString("notes"))
        } }
    }
    fun addService(name: String, cents: Long, description: String = "", duration: String = "", category: String = "") {
        append("services", JSONObject().put("id", UUID.randomUUID().toString()).put("name", name).put("cents", cents)
            .put("description", description).put("duration", duration).put("category", category))
    }
    fun addIncome(service: String, cents: Long, date: String, payment: String = "", notes: String = "") {
        append("incomes", JSONObject().put("service", service).put("cents", cents).put("date", date).put("payment", payment).put("notes", notes))
    }
    private fun read(key: String) = JSONArray(preferences.getString(key, "[]"))
    private fun append(key: String, value: JSONObject) { preferences.edit().putString(key, read(key).put(value).toString()).apply() }
}

fun parseCents(value: String): Long? = try {
    val normalized = value.trim().replace(',', '.')
    if (!normalized.matches(Regex("[0-9]+(?:\\.[0-9]{1,2})?"))) null
    else BigDecimal(normalized).movePointRight(2).longValueExact().takeIf { it > 0 }
} catch (_: ArithmeticException) { null } catch (_: NumberFormatException) { null }
