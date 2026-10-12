package br.com.mnunesnails.financas

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.Editable
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import br.com.mnunesnails.financas.data.FinanceRepository
import br.com.mnunesnails.financas.data.Income
import br.com.mnunesnails.financas.data.parseCents
import br.com.mnunesnails.financas.ui.IconView
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    // Resource qualifiers select the palette when Android changes the system theme.
    private val pink get() = getColor(R.color.accent)
    private val actionColor get() = getColor(R.color.action)
    private val onAction get() = getColor(R.color.on_action)
    private val palePink get() = getColor(R.color.accent_surface)
    private val ink get() = getColor(R.color.text_primary)
    private val muted get() = getColor(R.color.text_secondary)
    private val line get() = getColor(R.color.outline)
    private val paper get() = getColor(R.color.background)
    private val surface get() = getColor(R.color.surface)
    private val green get() = getColor(R.color.positive)
    private val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private lateinit var repository: FinanceRepository
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var screen = "home"
    private var returnScreen = "home"
    private val serviceDraft = linkedMapOf<String, String>()
    private val incomeDraft = linkedMapOf<String, String>()
    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() { leaveScreen() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = FinanceRepository(this)
        screen = savedInstanceState?.getString("screen") ?: "home"
        returnScreen = savedInstanceState?.getString("returnScreen") ?: "home"
        restoreDraft(savedInstanceState?.getBundle("serviceDraft"), serviceDraft)
        restoreDraft(savedInstanceState?.getBundle("incomeDraft"), incomeDraft)
        onBackPressedDispatcher.addCallback(this, backCallback)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = resources.getBoolean(R.bool.light_system_bars)
            isAppearanceLightNavigationBars = resources.getBoolean(R.bool.light_system_bars)
        }
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("screen", screen)
        outState.putString("returnScreen", returnScreen)
        outState.putBundle("serviceDraft", Bundle().apply { serviceDraft.forEach { (key, value) -> putString(key, value) } })
        outState.putBundle("incomeDraft", Bundle().apply { incomeDraft.forEach { (key, value) -> putString(key, value) } })
        super.onSaveInstanceState(outState)
    }

    private fun restoreDraft(bundle: Bundle?, draft: MutableMap<String, String>) {
        bundle?.keySet()?.forEach { draft[it] = bundle.getString(it).orEmpty() }
    }

    private fun render() {
        backCallback.isEnabled = screen != "home"
        root = column().apply { setBackgroundColor(paper) }
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime())
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom)
            insets
        }
        val form = screen.endsWith("Form")
        root.addView(if (screen == "home") brandHeader() else header(when (screen) {
            "serviceForm" -> "Cadastrar Serviço"
            "incomeForm" -> "Registrar Receita"
            "services" -> "Serviços"
            else -> "Receitas"
        }))
        content = column().apply {
            setPadding(dp(16), dp(12), dp(16), dp(20))
            isFocusableInTouchMode = true
        }
        root.addView(ScrollView(this).apply { isFillViewport = true; isVerticalScrollBarEnabled = false; addView(content) }, LinearLayout.LayoutParams(-1, 0, 1f))
        when (screen) {
            "home" -> home()
            "services" -> services()
            "incomes" -> incomes()
            "serviceForm" -> serviceForm()
            "incomeForm" -> incomeForm()
        }
        if (!form) root.addView(bottomNavigation())
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
    }

    private fun go(destination: String) { hideKeyboard(); screen = destination; render() }
    private fun leaveScreen() {
        if (screen.endsWith("Form")) {
            if (screen == "serviceForm") serviceDraft.clear() else incomeDraft.clear()
            go(returnScreen)
        } else go("home")
    }
    private fun openService() { returnScreen = screen; serviceDraft.clear(); go("serviceForm") }
    private fun openIncome() {
        if (repository.services().isEmpty()) {
            AlertDialog.Builder(this).setTitle("Cadastre seu primeiro serviço")
                .setMessage("Para registrar uma receita, adicione primeiro um serviço ao seu catálogo.")
                .setNegativeButton("Agora não", null).setPositiveButton("Cadastrar serviço") { _, _ -> openService() }.show()
        } else {
            returnScreen = screen
            incomeDraft.clear()
            incomeDraft["date"] = LocalDate.now().toString()
            go("incomeForm")
        }
    }

    private fun brandHeader(): View = row().apply {
        setPadding(dp(8), dp(2), dp(8), 0)
        addView(iconButton("menu", "Abrir menu") { showMenu() }, LinearLayout.LayoutParams(dp(48), dp(56)))
        val brand = column().apply {
            gravity = Gravity.CENTER
            addView(IconView(this@MainActivity, "flower", pink), LinearLayout.LayoutParams(dp(36), dp(34)))
            addView(label("MNunesnail Finanças", 15f, ink, true).apply { gravity = Gravity.CENTER })
        }
        addView(brand, LinearLayout.LayoutParams(0, dp(64), 1f))
        addView(iconButton("user", "Meu salão") {
            AlertDialog.Builder(this@MainActivity).setTitle("MNunes Nails")
                .setMessage("Seu salão, suas conquistas.\n\n${repository.services().size} serviços cadastrados\n${repository.incomes().size} recebimentos registrados\n\nOs dados ficam salvos neste dispositivo.")
                .setPositiveButton("Concluir", null).show()
        }, LinearLayout.LayoutParams(dp(48), dp(56)))
    }

    private fun header(title: String): View = column().apply {
        addView(row().apply {
            addView(iconButton("back", "Voltar") { leaveScreen() }, LinearLayout.LayoutParams(dp(48), dp(56)))
            addView(label(title, 18f, ink, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, dp(56), 1f))
            addView(Space(this@MainActivity), LinearLayout.LayoutParams(dp(48), 1))
        })
        addView(View(this@MainActivity).apply { setBackgroundColor(line) }, LinearLayout.LayoutParams(-1, dp(1)))
    }

    private fun home() {
        content.addView(label("Olá!", 27f, ink, true))
        content.addView(label("Veja o resumo financeiro do seu salão.", 13f, muted))
        val all = repository.incomes()
        val month = YearMonth.now()
        val current = total(all.filter { YearMonth.from(LocalDate.parse(it.date)) == month })
        val previous = total(all.filter { YearMonth.from(LocalDate.parse(it.date)) == month.minusMonths(1) })
        val hero = row().apply {
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(getColor(R.color.hero_start), getColor(R.color.hero_end))).apply { cornerRadius = dp(12).toFloat() }
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }
        val figures = column().apply {
            addView(label("Receitas do mês", 14f))
            addView(label(currency.format(current), 27f, ink, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
            val comparison = if (previous.signum() > 0) {
                val percent = (current - previous).multiply(BigDecimal(100)).divide(previous, 0, RoundingMode.HALF_UP)
                "${if (percent.signum() >= 0) "↑ +" else "↓ "}$percent% em relação ao mês anterior"
            } else "${all.count { it.date.startsWith(month.toString()) }} recebimentos neste mês"
            addView(label(comparison, 11f, if (current >= previous) green else muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
        }
        hero.addView(figures, LinearLayout.LayoutParams(0, -2, 1f))
        hero.addView(IconView(this, "chart", pink), LinearLayout.LayoutParams(dp(38), dp(44)))
        content.addView(hero, full(top = 12))
        val stats = row()
        stats.addView(stat("money", "Total de receitas", currency.format(total(all))), LinearLayout.LayoutParams(0, -1, 1f).apply { marginEnd = dp(8) })
        stats.addView(stat("calendar", "Serviços realizados", all.size.toString()), LinearLayout.LayoutParams(0, -1, 1f))
        content.addView(stats, full(top = 10))
        content.addView(label("Ações rápidas", 16f, ink, true), full(top = 20, bottom = 10))
        val actions = row()
        listOf(Triple("plus", "Registrar\nReceita", { openIncome() }), Triple("list", "Cadastrar\nServiço", { openService() }), Triple("search", "Consultar\nRegistros", { go("incomes") })).forEachIndexed { index, (icon, title, click) ->
            actions.addView(column().apply {
                gravity = Gravity.CENTER
                background = touch(palePink, 12)
                setPadding(dp(6), dp(12), dp(6), dp(12))
                addView(iconBubble(icon, actionColor, onAction, 34))
                addView(label(title, 13f).apply { gravity = Gravity.CENTER }, full(top = 6))
                setOnClickListener { click() }
                contentDescription = title.replace('\n', ' ')
                isFocusable = true
            }, LinearLayout.LayoutParams(0, -1, 1f).apply { if (index < 2) marginEnd = dp(8) })
        }
        content.addView(actions)
        content.addView(row().apply {
            addView(label("Últimos lançamentos", 16f, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
            addView(label("Ver todos", 13f, pink).apply {
                gravity = Gravity.CENTER; minHeight = dp(48); setPadding(dp(10), 0, 0, 0)
                setOnClickListener { go("incomes") }; isFocusable = true
            })
        }, full(top = 8))
        val recent = all.asReversed().sortedByDescending { it.date }.take(4)
        if (recent.isEmpty()) emptyState("Seu primeiro lançamento", "Registre uma receita para acompanhar os resultados do seu salão.")
        else recent.forEach { incomeRow(it) }
    }

    private fun stat(icon: String, title: String, value: String): View = row().apply {
        background = shape(surface, 10)
        elevation = dp(2).toFloat()
        setPadding(dp(10), dp(12), dp(10), dp(12))
        addView(iconBubble(icon, palePink, pink, 32))
        addView(column().apply {
            addView(label(title, 10f))
            addView(label(value, 15f, ink, true))
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(8) })
    }

    private fun services() {
        content.addView(label("Seu catálogo de cuidados", 23f, ink, true))
        content.addView(label("Serviços e valores, organizados em um só lugar.", 13f, muted), full(top = 4, bottom = 16))
        content.addView(button("Cadastrar serviço", "plus", true) { openService() }, full(bottom = 16))
        val items = repository.services()
        if (items.isEmpty()) emptyState("Seu catálogo começa aqui", "Cadastre manicure, pedicure e os outros serviços que você oferece.")
        items.forEach { service ->
            val card = row().apply {
                background = touch(surface, 10); setPadding(dp(12), dp(14), dp(12), dp(14))
                addView(iconBubble("nail", palePink, pink, 38))
                addView(column().apply {
                    addView(label(service.name, 15f, ink, true))
                    addView(label(listOf(service.category, service.duration).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Serviço cadastrado" }, 12f, muted))
                }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10); marginEnd = dp(8) })
                addView(label(money(service.cents), 14f, pink, true))
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity).setTitle(service.name)
                        .setMessage(listOf(money(service.cents), service.description, service.duration.takeIf { it.isNotBlank() }?.let { "Duração: $it" }.orEmpty(), service.category).filter { it.isNotBlank() }.joinToString("\n\n"))
                        .setPositiveButton("Fechar", null).show()
                }
                isFocusable = true
            }
            content.addView(card, full(bottom = 8))
        }
    }

    private fun incomes() {
        content.addView(label("Seus recebimentos", 23f, ink, true))
        content.addView(label("Acompanhe cada conquista do seu salão.", 13f, muted), full(top = 4, bottom = 16))
        content.addView(button("Registrar receita", "plus", true) { openIncome() }, full(bottom = 16))
        val items = repository.incomes().asReversed().sortedByDescending { it.date }
        if (items.isEmpty()) emptyState("Nenhum recebimento ainda", "Seus lançamentos aparecerão aqui após registrar a primeira receita.")
        items.forEach { incomeRow(it) }
    }

    private fun incomeRow(income: Income) {
        val card = row().apply {
            background = touch(surface, 8)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            minimumHeight = dp(54)
            addView(iconBubble("nail", palePink, pink, 34))
            addView(column().apply {
                addView(label(income.service, 13f, ink, true))
                addView(label(LocalDate.parse(income.date).format(dateFormat), 11f, muted))
            }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10); marginEnd = dp(8) })
            addView(label(money(income.cents), 13f, green, true))
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity).setTitle(income.service)
                    .setMessage(listOf(money(income.cents), LocalDate.parse(income.date).format(dateFormat), income.payment, income.notes).filter { it.isNotBlank() }.joinToString("\n\n"))
                    .setPositiveButton("Fechar", null).show()
            }
            isFocusable = true
        }
        content.addView(card, full(bottom = 3))
    }

    private fun serviceForm() {
        val name = input("Nome do serviço", "Ex.: Manicure, Pedicure, Alongamento...", "nail", serviceDraft, "name", required = true)
        input("Descrição (opcional)", "Descreva o serviço realizado", "note", serviceDraft, "description", multiline = true)
        val price = input("Valor do serviço", "0,00", "money", serviceDraft, "price", required = true, decimal = true)
        input("Duração (opcional)", "Ex.: 1h, 1h30", "clock", serviceDraft, "duration")
        fieldLabel("Categoria (opcional)")
        val category = select("tag", serviceDraft["category"].orEmpty(), "Selecione uma categoria")
        category.first.setOnClickListener {
            val choices = arrayOf("Manicure", "Pedicure", "Alongamento", "Nail art", "Cuidados", "Outros")
            choose("Categoria", choices) { serviceDraft["category"] = choices[it]; setSelection(category.second, choices[it]) }
        }
        content.addView(category.first, full(bottom = 20))
        content.addView(row().apply {
            background = shape(palePink, 6); setPadding(dp(12), dp(12), dp(12), dp(12))
            addView(IconView(this@MainActivity, "info", pink), LinearLayout.LayoutParams(dp(22), dp(22)))
            addView(label("O serviço cadastrado ficará disponível para ser selecionado no registro de receitas.", 12f, pink), LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        }, full(bottom = 10))
        formActions {
            val cents = parseCents(price.text.toString())
            when {
                name.text.toString().isBlank() -> { name.error = "Informe o nome do serviço"; name.requestFocus() }
                cents == null -> { price.error = "Informe um valor positivo com até 2 casas decimais"; price.requestFocus() }
                else -> {
                    repository.addService(name.text.toString().trim(), cents, serviceDraft["description"].orEmpty().trim(), serviceDraft["duration"].orEmpty().trim(), serviceDraft["category"].orEmpty())
                    serviceDraft.clear(); go(returnScreen)
                    Toast.makeText(this, "Serviço cadastrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun incomeForm() {
        val services = repository.services()
        fieldLabel("Serviço realizado", true)
        val chosen = services.find { it.id == incomeDraft["serviceId"] }
        val service = select("nail", chosen?.name.orEmpty(), "Selecione o serviço")
        content.addView(service.first, full(bottom = 20))
        val price = input("Valor recebido", "0,00", "money", incomeDraft, "price", required = true, decimal = true)
        service.first.setOnClickListener {
            choose("Serviço realizado", services.map { it.name }.toTypedArray()) { index ->
                incomeDraft["serviceId"] = services[index].id
                setSelection(service.second, services[index].name)
                price.setText(BigDecimal.valueOf(services[index].cents, 2).toPlainString().replace('.', ','))
            }
        }
        fieldLabel("Data de recebimento", true)
        val date = LocalDate.parse(incomeDraft["date"] ?: LocalDate.now().toString())
        val dateControl = select("calendar", date.format(dateFormat), "", chevron = false)
        dateControl.first.setOnClickListener {
            val current = LocalDate.parse(incomeDraft["date"] ?: LocalDate.now().toString())
            DatePickerDialog(this, { _, year, month, day ->
                val newDate = LocalDate.of(year, month + 1, day)
                incomeDraft["date"] = newDate.toString()
                setSelection(dateControl.second, newDate.format(dateFormat))
            }, current.year, current.monthValue - 1, current.dayOfMonth).show()
        }
        content.addView(dateControl.first, full(bottom = 20))
        fieldLabel("Forma de pagamento", true)
        val payment = select("card", incomeDraft["payment"].orEmpty(), "Selecione a forma de pagamento")
        payment.first.setOnClickListener {
            val choices = arrayOf("Pix", "Dinheiro", "Cartão de débito", "Cartão de crédito", "Transferência")
            choose("Forma de pagamento", choices) { incomeDraft["payment"] = choices[it]; setSelection(payment.second, choices[it]) }
        }
        content.addView(payment.first, full(bottom = 20))
        input("Observações (opcional)", "Adicione uma observação sobre o recebimento", "note", incomeDraft, "notes", multiline = true)
        formActions {
            val selectedService = services.find { it.id == incomeDraft["serviceId"] }
            val cents = parseCents(price.text.toString())
            when {
                selectedService == null -> { service.second.error = "Selecione o serviço"; service.first.requestFocus(); Toast.makeText(this, "Selecione o serviço realizado", Toast.LENGTH_SHORT).show() }
                cents == null -> { price.error = "Informe um valor positivo com até 2 casas decimais"; price.requestFocus() }
                incomeDraft["payment"].isNullOrBlank() -> { payment.second.error = "Selecione a forma de pagamento"; payment.first.requestFocus(); Toast.makeText(this, "Selecione a forma de pagamento", Toast.LENGTH_SHORT).show() }
                else -> {
                    repository.addIncome(selectedService.name, cents, incomeDraft["date"] ?: LocalDate.now().toString(), incomeDraft["payment"].orEmpty(), incomeDraft["notes"].orEmpty().trim())
                    incomeDraft.clear(); go(returnScreen)
                    Toast.makeText(this, "Receita registrada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun input(title: String, hintText: String, icon: String, draft: MutableMap<String, String>, key: String, required: Boolean = false, decimal: Boolean = false, multiline: Boolean = false): EditText {
        fieldLabel(title, required)
        val box = column().apply { background = shape(Color.TRANSPARENT, 5, line); setPadding(dp(12), dp(2), dp(12), dp(2)) }
        val fieldRow = row().apply { if (multiline) gravity = Gravity.TOP }
        fieldRow.addView(if (decimal) label("R$", 16f).apply { gravity = Gravity.CENTER } else IconView(this, icon, ink), LinearLayout.LayoutParams(dp(24), dp(44)))
        val edit = EditText(this).apply {
            background = null; hint = hintText; textSize = 13f; setTextColor(ink); setHintTextColor(muted)
            setPadding(dp(12), dp(10), 0, dp(10))
            inputType = when { decimal -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; multiline -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES; else -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
            if (multiline) { minLines = 2; gravity = Gravity.TOP } else setSingleLine(true)
            filters = arrayOf(InputFilter.LengthFilter(if (multiline) 200 else if (decimal) 20 else 100))
            setText(draft[key].orEmpty())
            contentDescription = title
        }
        fieldRow.addView(edit, LinearLayout.LayoutParams(0, if (multiline) dp(70) else -2, 1f))
        box.addView(fieldRow)
        val counter = label("${edit.text.length}/200", 11f, muted).apply { gravity = Gravity.END; setPadding(0, 0, 0, dp(6)) }
        if (multiline) box.addView(counter)
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { draft[key] = s.toString(); if (multiline) counter.text = "${s?.length ?: 0}/200" }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        edit.setOnFocusChangeListener { _, focused -> box.background = shape(Color.TRANSPARENT, 5, if (focused) pink else line) }
        content.addView(box, full(bottom = 20))
        return edit
    }

    private fun fieldLabel(title: String, required: Boolean = false) {
        content.addView(label(title + if (required) " *" else "", 13f).apply {
            if (required) text = SpannableString(text).apply { setSpan(ForegroundColorSpan(pink), length - 1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        }, full(bottom = 7))
    }

    private fun select(icon: String, value: String, placeholder: String, chevron: Boolean = true): Pair<LinearLayout, TextView> {
        val title = label(value.ifBlank { placeholder }, 13f, if (value.isBlank()) muted else ink)
        val box = row().apply {
            background = touch(Color.TRANSPARENT, 5, line); minimumHeight = dp(48)
            setPadding(dp(12), dp(6), dp(12), dp(6)); isFocusable = true
            addView(IconView(this@MainActivity, icon, ink), LinearLayout.LayoutParams(dp(24), dp(24)))
            addView(title, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12); marginEnd = dp(8) })
            if (chevron) addView(IconView(this@MainActivity, "down", ink), LinearLayout.LayoutParams(dp(18), dp(18)))
        }
        return box to title
    }
    private fun setSelection(label: TextView, value: String) { label.text = value; label.setTextColor(ink); label.error = null }
    private fun choose(title: String, items: Array<String>, onSelect: (Int) -> Unit) {
        hideKeyboard()
        AlertDialog.Builder(this).setTitle(title).setItems(items) { _, index -> onSelect(index) }.setNegativeButton("Cancelar", null).show()
    }
    private fun formActions(save: () -> Unit) {
        content.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))
        content.addView(row().apply {
            addView(button("Cancelar", "close", false) { leaveScreen() }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(10) })
            addView(button("Salvar", "save", true, save), LinearLayout.LayoutParams(0, dp(48), 1f))
        }, full(top = 12))
    }

    private fun bottomNavigation(): View = row().apply {
        background = shape(surface, 0); elevation = dp(8).toFloat()
        setPadding(dp(4), dp(6), dp(4), dp(8))
        listOf("home" to "Início", "list" to "Serviços", "plus" to "", "chart" to "Receitas", "more" to "Mais").forEachIndexed { index, (icon, title) ->
            val active = (screen == "home" && index == 0) || (screen == "services" && index == 1) || (screen == "incomes" && index == 3)
            addView(column().apply {
                gravity = Gravity.CENTER
                minimumHeight = dp(60)
                if (index == 2) addView(iconBubble("plus", actionColor, onAction, 54).apply { elevation = dp(5).toFloat() })
                else {
                    addView(IconView(this@MainActivity, icon, if (active) pink else muted), LinearLayout.LayoutParams(dp(23), dp(23)))
                    addView(label(title, 10f, if (active) pink else muted, active), full(top = 3))
                    (getChildAt(1) as TextView).gravity = Gravity.CENTER
                }
                background = touch(Color.TRANSPARENT, 8)
                contentDescription = if (index == 2) "Novo lançamento" else title + if (active) ", selecionado" else ""
                isFocusable = true
                setOnClickListener { when (index) { 0 -> go("home"); 1 -> go("services"); 2 -> openIncome(); 3 -> go("incomes"); else -> showMenu() } }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
    }
    private fun showMenu() {
        choose("MNunes Nails", arrayOf("Início", "Serviços", "Receitas", "Cadastrar serviço", "Registrar receita")) {
            when (it) { 0 -> go("home"); 1 -> go("services"); 2 -> go("incomes"); 3 -> openService(); 4 -> openIncome() }
        }
    }
    private fun emptyState(title: String, description: String) {
        content.addView(column().apply {
            background = shape(surface, 10); gravity = Gravity.CENTER; setPadding(dp(20), dp(24), dp(20), dp(24))
            addView(iconBubble("nail", palePink, pink, 42))
            addView(label(title, 15f, ink, true).apply { gravity = Gravity.CENTER }, full(top = 10))
            addView(label(description, 13f, muted).apply { gravity = Gravity.CENTER }, full(top = 5))
        }, full())
    }
    private fun button(title: String, icon: String, filled: Boolean, click: () -> Unit): View = row().apply {
        gravity = Gravity.CENTER; minimumHeight = dp(48)
        background = touch(if (filled) actionColor else Color.TRANSPARENT, 6, if (filled) null else pink)
        addView(IconView(this@MainActivity, icon, if (filled) onAction else pink), LinearLayout.LayoutParams(dp(22), dp(22)))
        addView(label(title, 15f, if (filled) onAction else pink, true), LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(10) })
        contentDescription = title; isFocusable = true; setOnClickListener { click() }
    }
    private fun iconButton(icon: String, title: String, click: () -> Unit): View = FrameLayout(this).apply {
        background = touch(Color.TRANSPARENT, 24)
        addView(IconView(this@MainActivity, icon, ink), FrameLayout.LayoutParams(dp(23), dp(23), Gravity.CENTER))
        contentDescription = title; isFocusable = true; setOnClickListener { click() }
    }
    private fun iconBubble(icon: String, backgroundColor: Int, foreground: Int, size: Int): View = FrameLayout(this).apply {
        background = shape(backgroundColor, size / 2)
        addView(IconView(this@MainActivity, icon, foreground), FrameLayout.LayoutParams(dp(size * 3 / 5), dp(size * 3 / 5), Gravity.CENTER))
        layoutParams = LinearLayout.LayoutParams(dp(size), dp(size))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private fun shape(color: Int, radius: Int, border: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); if (border != null) setStroke(dp(1), border)
    }
    private fun touch(color: Int, radius: Int, border: Int? = null) = RippleDrawable(ColorStateList.valueOf(getColor(R.color.ripple)), shape(color, radius, border), shape(Color.WHITE, radius))
    private fun label(value: String, size: Float, color: Int = ink, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); includeFontPadding = false
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        setLineSpacing(dp(2).toFloat(), 1f)
    }
    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun full(top: Int = 0, bottom: Int = 0) = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top); bottomMargin = dp(bottom) }
    private fun money(cents: Long) = currency.format(BigDecimal.valueOf(cents, 2))
    private fun total(items: List<Income>) = items.fold(BigDecimal.ZERO) { sum, entry -> sum + BigDecimal.valueOf(entry.cents, 2) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun hideKeyboard() { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(window.decorView.windowToken, 0) }
}
