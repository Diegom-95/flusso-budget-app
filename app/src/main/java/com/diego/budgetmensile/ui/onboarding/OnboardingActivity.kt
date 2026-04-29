package com.diego.budgetmensile.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import androidx.viewpager2.widget.ViewPager2
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.Income
import com.diego.budgetmensile.utils.extractMonth
import com.diego.budgetmensile.utils.extractYear
import com.diego.budgetmensile.utils.todayString
import kotlinx.coroutines.launch
import java.util.Calendar

data class OnboardingPage(
    val emoji: String, val title: String, val desc: String,
    val showSalaryInput: Boolean = false
)

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.VH>() {

    var salaryInput: Double? = null

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji  = view.findViewById<TextView>(R.id.tvEmoji)
        val tvTitle  = view.findViewById<TextView>(R.id.tvTitle)
        val tvDesc   = view.findViewById<TextView>(R.id.tvDesc)
        val tilSalary = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSalary)
        val etSalary  = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSalary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val page = pages[position]
        holder.tvEmoji.text = page.emoji
        holder.tvTitle.text = page.title
        holder.tvDesc.text  = page.desc
        if (page.showSalaryInput) {
            holder.tilSalary.visibility = View.VISIBLE
            holder.etSalary.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) { salaryInput = s.toString().toDoubleOrNull() }
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            })
        } else {
            holder.tilSalary.visibility = View.GONE
        }
    }

    override fun getItemCount() = pages.size
}

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private lateinit var dotsContainer: LinearLayout
    private lateinit var adapter: OnboardingAdapter

    private val pages = listOf(
        OnboardingPage(
            emoji = "👋",
            title = "Benvenuto in Flusso",
            desc  = "Il tuo tracker personale per entrate, spese, risparmi e debiti.\nSemplice, veloce, privato."
        ),
        OnboardingPage(
            emoji = "📊",
            title = "Tutto sotto controllo",
            desc  = "• Dashboard con saldo mensile\n• Grafici per categoria\n• Riepilogo annuale\n• Obiettivi di risparmio\n• Budget per categoria\n• Spese ricorrenti"
        ),
        OnboardingPage(
            emoji = "💰",
            title = "Iniziamo!",
            desc  = "Quanto guadagni al mese? Ti aiutiamo a impostare la prima entrata così la dashboard è subito pronta.",
            showSalaryInput = true
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // Applica tema salvato
        val prefs = getSharedPreferences("flusso_prefs", Context.MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.getBoolean("dark_mode", true)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager      = findViewById(R.id.viewPager)
        btnNext        = findViewById(R.id.btnNext)
        btnSkip        = findViewById(R.id.btnSkip)
        dotsContainer  = findViewById(R.id.dotsContainer)

        adapter = OnboardingAdapter(pages)
        viewPager.adapter = adapter

        buildDots(0)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                buildDots(position)
                btnNext.text = if (position == pages.lastIndex) "Inizia →" else "Avanti →"
                btnSkip.visibility = if (position == pages.lastIndex) View.GONE else View.VISIBLE
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < pages.lastIndex) {
                viewPager.currentItem++
            } else {
                finish()
            }
        }
        btnSkip.setOnClickListener { finish() }
    }

    override fun finish() {
        // Salva stipendio se inserito
        val salary = adapter.salaryInput
        if (salary != null && salary > 0) {
            val repo = BudgetRepository(application)
            val cal  = Calendar.getInstance()
            val today = todayString()
            lifecycleScope.launch {
                repo.insertIncome(Income(
                    date = today, description = "Stipendio", category = "Stipendio",
                    amount = salary, isActive = true,
                    month = today.extractMonth(), year = today.extractYear()
                ))
            }
        }
        // Segna onboarding come completato
        getSharedPreferences("flusso_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("onboarding_done", true).apply()

        startActivity(Intent(this, MainActivity::class.java))
        super.finish()
    }

    private fun buildDots(current: Int) {
        dotsContainer.removeAllViews()
        pages.forEachIndexed { i, _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    if (i == current) 24.dp else 8.dp, 8.dp
                ).also { it.setMargins(4.dp, 0, 4.dp, 0) }
                setBackgroundResource(
                    if (i == current) R.drawable.dot_active else R.drawable.dot_inactive
                )
            }
            dotsContainer.addView(dot)
        }
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()

    companion object {
        fun shouldShow(context: Context): Boolean {
            val prefs = context.getSharedPreferences("flusso_prefs", Context.MODE_PRIVATE)
            return !prefs.getBoolean("onboarding_done", false)
        }
    }
}
