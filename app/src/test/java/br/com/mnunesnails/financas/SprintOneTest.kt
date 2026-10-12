package br.com.mnunesnails.financas

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import br.com.mnunesnails.financas.data.FinanceRepository
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowDialog
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "pt-rBR-notnight")
class SprintOneTest {
    private lateinit var controller: ActivityController<MainActivity>
    private val activity get() = controller.get()
    private val repository get() = FinanceRepository(RuntimeEnvironment.getApplication())

    @Before fun reset() {
        RuntimeEnvironment.getApplication().getSharedPreferences("finance", Context.MODE_PRIVATE).edit().clear().commit()
        controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    }

    @After fun close() { controller.pause().stop().destroy() }

    @Test fun emptyScreensAndBackNavigation() {
        assertText("Seu primeiro lançamento")
        assertText("Serviços cadastrados: 0")
        click("Serviços")
        assertText("Seu catálogo começa aqui")
        activity.onBackPressedDispatcher.onBackPressed()
        assertText("Olá!")
        click("Receitas")
        assertText("Nenhum recebimento ainda")
        activity.onBackPressedDispatcher.onBackPressed()
        assertText("Olá!")
    }

    @Test fun serviceValidationOptionalFieldsAndPersistence() {
        click("Cadastrar Serviço")
        click("Salvar")
        assertNotNull(edit("Nome do serviço").error)
        assertTrue(repository.services().isEmpty())
        edit("Nome do serviço").setText("Manicure")
        for (invalid in listOf("0", "45,999")) {
            edit("Valor do serviço").setText(invalid)
            click("Salvar")
            assertNotNull(edit("Valor do serviço").error)
            assertTrue(repository.services().isEmpty())
        }
        edit("Valor do serviço").setText("45,00")
        edit("Descrição (opcional)").setText("Esmaltação e cutículas")
        edit("Duração (opcional)").setText("1h")
        choose("Selecione uma categoria", 0)
        click("Salvar")
        shadowOf(Looper.getMainLooper()).idle()
        val service = repository.services().single()
        assertEquals(4500L, service.cents)
        assertEquals("Esmaltação e cutículas", service.description)
        assertEquals("1h", service.duration)
        assertEquals("Manicure", service.category)
        assertText("Serviços cadastrados: 1")
        restart()
        click("Serviços")
        assertText("Manicure")
        clickText("Manicure")
        assertTrue(shadowOf(ShadowAlertDialog.getLatestAlertDialog()).message.toString().contains("Esmaltação e cutículas"))
    }

    @Test fun incomeRequiresServiceAndPaymentAndSavesDetails() {
        repository.addService("Manicure", 4500)
        click("Registrar Receita")
        click("Salvar")
        assertTrue(repository.incomes().isEmpty())
        choose("Selecione o serviço", 0)
        assertEquals("45,00", edit("Valor recebido").text.toString())
        click("Salvar")
        assertTrue(repository.incomes().isEmpty())
        choose("Selecione a forma de pagamento", 0)
        edit("Observações (opcional)").setText("Cliente retornou")
        click("Salvar")
        val income = repository.incomes().single()
        assertEquals(4500L, income.cents)
        assertEquals("Pix", income.payment)
        assertEquals("Cliente retornou", income.notes)
        assertEquals(LocalDate.now().toString(), income.date)
        assertTrue(texts().any { it.replace('\u00a0', ' ').contains("45,00") })
        restart()
        click("Receitas")
        assertText("Manicure")
        clickText("Manicure")
        assertTrue(shadowOf(ShadowAlertDialog.getLatestAlertDialog()).message.toString().contains("Cliente retornou"))
    }

    @Test fun previousMonthDateUpdatesMonthlyComparison() {
        repository.addService("Manicure", 4500)
        repository.addIncome("Manicure", 9000, LocalDate.now().toString(), "Pix")
        click("Registrar Receita")
        choose("Selecione o serviço", 0)
        choose("Selecione a forma de pagamento", 0)
        clickText(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        val picker = ShadowDialog.getLatestDialog() as DatePickerDialog
        val previous = LocalDate.now().minusMonths(1)
        picker.updateDate(previous.year, previous.monthValue - 1, previous.dayOfMonth)
        picker.getButton(DatePickerDialog.BUTTON_POSITIVE).performClick()
        shadowOf(Looper.getMainLooper()).idle()
        click("Salvar")
        assertEquals(previous.toString(), repository.incomes().last().date)
        assertText("↑ +100% em relação ao mês anterior")
    }

    @Test fun cancelAndCharacterLimits() {
        click("Cadastrar Serviço")
        edit("Nome do serviço").setText("Não salvar")
        edit("Descrição (opcional)").setText("a".repeat(201))
        assertEquals(200, edit("Descrição (opcional)").length())
        assertText("200/200")
        click("Cancelar")
        assertTrue(repository.services().isEmpty())
        repository.addService("Manicure", 4500)
        click("Registrar Receita")
        choose("Selecione o serviço", 0)
        edit("Observações (opcional)").setText("b".repeat(201))
        assertEquals(200, edit("Observações (opcional)").length())
        assertText("200/200")
        click("Cancelar")
        assertTrue(repository.incomes().isEmpty())
    }

    @Test fun draftSurvivesRotationAndThemeChange() {
        click("Cadastrar Serviço")
        edit("Nome do serviço").setText("Manicure")
        edit("Valor do serviço").setText("45,00")
        edit("Descrição (opcional)").setText("Rascunho")
        choose("Selecione uma categoria", 0)
        controller.configurationChange(Configuration(activity.resources.configuration).apply {
            orientation = Configuration.ORIENTATION_LANDSCAPE
        })
        assertEquals("Manicure", edit("Nome do serviço").text.toString())
        controller.configurationChange(Configuration(activity.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
        })
        assertFalse(activity.resources.getBoolean(R.bool.light_system_bars))
        assertEquals("45,00", edit("Valor do serviço").text.toString())
        assertEquals("Rascunho", edit("Descrição (opcional)").text.toString())
        assertText("Manicure")
        assertTrue(repository.services().isEmpty())
    }

    @Test fun incomeDraftAndSelectedTabSurviveRecreation() {
        repository.addService("Manicure", 4500)
        click("Receitas")
        restart(restore = true)
        assertText("Seus recebimentos")
        click("Registrar receita")
        choose("Selecione o serviço", 0)
        choose("Selecione a forma de pagamento", 0)
        edit("Valor recebido").setText("50,00")
        edit("Observações (opcional)").setText("Rascunho da receita")
        restart(restore = true)
        assertText("Manicure")
        assertText("Pix")
        assertEquals("50,00", edit("Valor recebido").text.toString())
        assertEquals("Rascunho da receita", edit("Observações (opcional)").text.toString())
        assertTrue(repository.incomes().isEmpty())
    }

    @Test fun legacyRecordsRemainReadable() {
        RuntimeEnvironment.getApplication().getSharedPreferences("finance", Context.MODE_PRIVATE).edit()
            .putString("services", """[{"id":"old","name":"Manicure","cents":4500}]""")
            .putString("incomes", """[{"service":"Manicure","cents":4500,"date":"2025-01-05"}]""").commit()
        assertEquals("", repository.services().single().description)
        assertEquals("", repository.incomes().single().payment)
        assertEquals(4500L, repository.incomes().single().cents)
    }

    private fun restart(restore: Boolean = false) {
        val state = Bundle()
        controller.pause().saveInstanceState(state).stop().destroy()
        controller = Robolectric.buildActivity(MainActivity::class.java)
        if (restore) controller.setup(state) else controller.setup()
    }
    private fun nodes(view: View = activity.window.decorView): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { nodes(view.getChildAt(it)) } else emptyList()
    private fun texts() = nodes().filterIsInstance<TextView>().map { it.text.toString() }
    private fun assertText(value: String) { assertTrue("Missing text: $value", value in texts()) }
    private fun edit(title: String) = nodes().filterIsInstance<EditText>().first { it.contentDescription == title }
    private fun click(description: String) {
        nodes().first { it.isClickable && it.contentDescription?.toString() == description }.performClick()
        shadowOf(Looper.getMainLooper()).idle()
    }
    private fun clickText(text: String) {
        var view = nodes().filterIsInstance<TextView>().first { it.text.toString() == text } as View
        while (!view.isClickable) view = view.parent as View
        view.performClick()
        shadowOf(Looper.getMainLooper()).idle()
    }
    private fun choose(placeholder: String, index: Int) {
        clickText(placeholder)
        ShadowAlertDialog.getLatestAlertDialog().listView.performItemClick(null, index, index.toLong())
        shadowOf(Looper.getMainLooper()).idle()
    }
}
