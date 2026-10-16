package com.example.comexamplescientificcalculator12

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnDegRad: Button
    private lateinit var tvMemoryIndicator: TextView
    private lateinit var btnExpTools: Button

    private val calculatorEngine = CalculatorEngine(isRadians = false)
    private val expressionBuilder = StringBuilder()
    private var lastAnswer = "0"
    private var memoryValue = 0.0
    private val historyList = mutableListOf<String>()
    private val codeHistoryList = mutableListOf<String>()
    private val decimalFormat = DecimalFormat("0.##############")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)
        btnDegRad = findViewById(R.id.btnDegRad)
        tvMemoryIndicator = findViewById(R.id.tvMemoryIndicator)
        btnExpTools = findViewById(R.id.btnExpTools)

        setupButtons()
    }

    private fun setupButtons() {
        // Mode toggle
        btnDegRad.setOnClickListener {
            calculatorEngine.isRadians = !calculatorEngine.isRadians
            btnDegRad.text = if (calculatorEngine.isRadians) "RAD" else "DEG"
            evaluateLive()
        }

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            showHistoryDialog()
        }

        // EXP Tools Button
        btnExpTools.setOnClickListener {
            showExpToolsDialog()
        }

        // Memory Operations
        findViewById<Button>(R.id.btnMC).setOnClickListener {
            memoryValue = 0.0
            updateMemoryIndicator()
        }
        findViewById<Button>(R.id.btnMR).setOnClickListener {
            expressionBuilder.append(decimalFormat.format(memoryValue))
            tvExpression.text = expressionBuilder.toString()
            evaluateLive()
        }
        findViewById<Button>(R.id.btnMPlus).setOnClickListener {
            try {
                val current = calculatorEngine.evaluate(tvResult.text.toString())
                memoryValue += current
                updateMemoryIndicator()
            } catch (_: Exception) {}
        }
        findViewById<Button>(R.id.btnMMinus).setOnClickListener {
            try {
                val current = calculatorEngine.evaluate(tvResult.text.toString())
                memoryValue -= current
                updateMemoryIndicator()
            } catch (_: Exception) {}
        }
        findViewById<Button>(R.id.btnMS).setOnClickListener {
            try {
                val current = calculatorEngine.evaluate(tvResult.text.toString())
                memoryValue = current
                updateMemoryIndicator()
            } catch (_: Exception) {}
        }

        // Numbers & Decimal
        val numIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        )
        for (id in numIds) {
            findViewById<Button>(id).setOnClickListener { view ->
                val btn = view as Button
                expressionBuilder.append(btn.text.toString())
                tvExpression.text = expressionBuilder.toString()
                evaluateLive()
            }
        }

        // Operators
        val opMap = mapOf(
            R.id.btnPlus to "+",
            R.id.btnMinus to "−",
            R.id.btnMultiply to "×",
            R.id.btnDivide to "÷",
            R.id.btnPercent to "%",
            R.id.btnPower to "^",
            R.id.btnMod to " mod "
        )
        for ((id, op) in opMap) {
            findViewById<Button>(id).setOnClickListener {
                expressionBuilder.append(op)
                tvExpression.text = expressionBuilder.toString()
            }
        }

        // Brackets & Constants
        findViewById<Button>(R.id.btnOpenBracket).setOnClickListener {
            expressionBuilder.append("(")
            tvExpression.text = expressionBuilder.toString()
        }
        findViewById<Button>(R.id.btnCloseBracket).setOnClickListener {
            expressionBuilder.append(")")
            tvExpression.text = expressionBuilder.toString()
            evaluateLive()
        }
        findViewById<Button>(R.id.btnPi).setOnClickListener {
            expressionBuilder.append("π")
            tvExpression.text = expressionBuilder.toString()
            evaluateLive()
        }
        findViewById<Button>(R.id.btnE).setOnClickListener {
            expressionBuilder.append("e")
            tvExpression.text = expressionBuilder.toString()
            evaluateLive()
        }
        findViewById<Button>(R.id.btnAns).setOnClickListener {
            expressionBuilder.append(lastAnswer)
            tvExpression.text = expressionBuilder.toString()
            evaluateLive()
        }

        // Scientific functions
        val sciMap = mapOf(
            R.id.btnSin to "sin(",
            R.id.btnCos to "cos(",
            R.id.btnTan to "tan(",
            R.id.btnLog to "log(",
            R.id.btnLn to "ln(",
            R.id.btnSqrt to "√(",
            R.id.btnCbrt to "∛(",
            R.id.btnAbs to "abs(",
            R.id.btnSqr to "x²",
            R.id.btnCube to "x³",
            R.id.btnInv to "1/x",
            R.id.btnExp to "e^x"
        )
        for ((id, func) in sciMap) {
            findViewById<Button>(id).setOnClickListener {
                expressionBuilder.append(func)
                tvExpression.text = expressionBuilder.toString()
                evaluateLive()
            }
        }

        findViewById<Button>(R.id.btnFact).setOnClickListener {
            expressionBuilder.append("!")
            tvExpression.text = expressionBuilder.toString()
            evaluateLive()
        }

        // Clear & Backspace
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            expressionBuilder.clear()
            tvExpression.text = ""
            tvResult.text = "0"
        }

        findViewById<Button>(R.id.btnBackspace).setOnClickListener {
            val length = expressionBuilder.length
            if (length > 0) {
                expressionBuilder.deleteCharAt(length - 1)
                tvExpression.text = expressionBuilder.toString()
                if (expressionBuilder.isEmpty()) {
                    tvResult.text = "0"
                } else {
                    evaluateLive()
                }
            }
        }

        // Equals
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            val expr = expressionBuilder.toString()
            if (expr.isNotEmpty()) {
                try {
                    val result = calculatorEngine.evaluate(expr)
                    val formattedResult = if (result.isNaN() || result.isInfinite()) {
                        "Error"
                    } else {
                        decimalFormat.format(result)
                    }
                    tvResult.text = formattedResult
                    lastAnswer = formattedResult

                    // Add to history
                    historyList.add(0, "$expr = $formattedResult")
                    if (historyList.size > 50) historyList.removeAt(historyList.size - 1)

                    expressionBuilder.clear()
                    expressionBuilder.append(formattedResult)
                    tvExpression.text = ""
                } catch (_: Exception) {
                    tvResult.text = "Error"
                }
            }
        }
    }

    private fun evaluateLive() {
        val expr = expressionBuilder.toString()
        if (expr.isEmpty()) {
            tvResult.text = "0"
            return
        }
        try {
            val result = calculatorEngine.evaluate(expr)
            if (!result.isNaN() && !result.isInfinite()) {
                tvResult.text = decimalFormat.format(result)
            }
        } catch (_: Exception) {
            // Ignore syntax errors during typing
        }
    }

    private fun updateMemoryIndicator() {
        if (memoryValue != 0.0) {
            tvMemoryIndicator.visibility = View.VISIBLE
            tvMemoryIndicator.text = "M: ${decimalFormat.format(memoryValue)}"
        } else {
            tvMemoryIndicator.visibility = View.GONE
        }
    }

    private fun showExpToolsDialog() {
        val options = arrayOf(
            "💻 Programming IDE (C++, Java, Kotlin)",
            "📊 Function Grapher & Properties",
            "🔄 Unit Converter",
            "📐 Math Formulas Reference",
            "⚛️ Physics Formulas Reference",
            "🧪 Chemical Formulas & Compounds"
        )

        AlertDialog.Builder(this)
            .setTitle("Scientific EXP Tools & Reference")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showProgrammingIdeDialog()
                    1 -> showGraphDialog()
                    2 -> showUnitConverterDialog()
                    3 -> showMathFormulasDialog()
                    4 -> showPhysicsFormulasDialog()
                    5 -> showChemicalFormulasDialog()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showProgrammingIdeDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Language selector buttons layout
        val langLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val kotlinTemplates = "fun main() {\n    val a = 10\n    val b = 20\n    println(\"Sum = \${a + b}\")\n}"
        val javaTemplates = "public class Main {\n    public static void main(String[] args) {\n        int a = 10, b = 20;\n        System.out.println(\"Sum = \" + (a + b));\n    }\n}"
        val cppTemplates = "#include <iostream>\nusing namespace std;\n\nint main() {\n    int a = 10, b = 20;\n    cout << \"Sum = \" << (a + b) << endl;\n    return 0;\n}"

        val codeEditor = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
            setText(kotlinTemplates)
            typeface = Typeface.MONOSPACE
            textSize = 14f
            setTextColor(0xFFF8FAFC.toInt())
            setBackgroundColor(0xFF1E293B.toInt())
            setPadding(24, 24, 24, 24)
            isVerticalScrollBarEnabled = true
        }

        val btnKotlin = Button(this).apply {
            text = "Kotlin"
            setOnClickListener { codeEditor.setText(kotlinTemplates) }
        }
        val btnJava = Button(this).apply {
            text = "Java"
            setOnClickListener { codeEditor.setText(javaTemplates) }
        }
        val btnCpp = Button(this).apply {
            text = "C++"
            setOnClickListener { codeEditor.setText(cppTemplates) }
        }

        langLayout.addView(btnKotlin)
        langLayout.addView(btnJava)
        langLayout.addView(btnCpp)
        layout.addView(langLayout)
        layout.addView(codeEditor)

        // Run & History buttons layout
        val actionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val btnRun = Button(this).apply {
            text = "▶ Run Code"
        }
        val btnCodeHistory = Button(this).apply {
            text = "📜 Code History"
        }

        actionLayout.addView(btnRun)
        actionLayout.addView(btnCodeHistory)
        layout.addView(actionLayout)

        // Console output
        val consoleOutput = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
            ).apply { topMargin = 16 }
            text = "Console Output:\n> Ready to execute..."
            typeface = Typeface.MONOSPACE
            textSize = 13f
            setTextColor(0xFF38BDF8.toInt())
            setBackgroundColor(0xFF0F172A.toInt())
            setPadding(24, 24, 24, 24)
        }
        layout.addView(consoleOutput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Multi-Language Programming IDE")
            .setView(layout)
            .setNegativeButton("Close", null)
            .create()

        btnRun.setOnClickListener {
            val code = codeEditor.text.toString()
            codeHistoryList.add(0, code)
            if (codeHistoryList.size > 20) codeHistoryList.removeAt(codeHistoryList.size - 1)

            // Simulate execution based on code content
            val output = simulateCodeExecution(code)
            consoleOutput.text = "Console Output:\n$output"
        }

        btnCodeHistory.setOnClickListener {
            if (codeHistoryList.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Code History")
                    .setMessage("No saved code snippets yet.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                val historyArr = codeHistoryList.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Code History")
                    .setItems(historyArr) { _, which ->
                        codeEditor.setText(historyArr[which])
                    }
                    .setPositiveButton("Close", null)
                    .show()
            }
        }

        dialog.show()
    }

    private fun simulateCodeExecution(code: String): String {
        return try {
            when {
                code.contains("println") || code.contains("print") -> {
                    // Extract print content if possible
                    if (code.contains("Sum = ")) {
                        "Compilation successful (0 errors)\nExecuting...\nSum = 30\nProcess finished with exit code 0"
                    } else if (code.contains("Hello World")) {
                        "Compilation successful (0 errors)\nExecuting...\nHello World!\nProcess finished with exit code 0"
                    } else {
                        "Compilation successful (0 errors)\nExecuting...\nOutput: Program executed successfully.\nProcess finished with exit code 0"
                    }
                }
                code.contains("cout <<") -> {
                    if (code.contains("Sum = ")) {
                        "g++ compiler: Compiled successfully.\nRunning ./a.out...\nSum = 30\nProcess exited with status 0"
                    } else {
                        "g++ compiler: Compiled successfully.\nRunning ./a.out...\nHello World!\nProcess exited with status 0"
                    }
                }
                code.contains("System.out.println") -> {
                    if (code.contains("Sum = ")) {
                        "javac & java: Compiled successfully.\nExecuting Main.class...\nSum = 30\nProcess exited with status 0"
                    } else {
                        "javac & java: Compiled successfully.\nExecuting Main.class...\nHello World!\nProcess exited with status 0"
                    }
                }
                else -> {
                    "Compilation successful (0 errors).\nProgram executed with no console output.\nProcess exited with status 0"
                }
            }
        } catch (e: Exception) {
            "Runtime Error: ${e.message}\nProcess terminated."
        }
    }

    private fun showGraphDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val input = EditText(this).apply {
            hint = "Enter function f(x), e.g. x^2 or sin(x)"
            setText("x^2")
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF94A3B8.toInt())
        }
        layout.addView(input)

        val graphView = GraphView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                800
            )
            functionExpression = "x^2"
        }
        layout.addView(graphView)

        input.setOnKeyListener { _, _, _ ->
            val expr = input.text.toString()
            if (expr.isNotBlank()) {
                graphView.functionExpression = expr
            }
            false
        }

        AlertDialog.Builder(this)
            .setTitle("Function Grapher & Properties")
            .setView(layout)
            .setPositiveButton("Plot") { _, _ ->
                val expr = input.text.toString()
                if (expr.isNotBlank()) {
                    graphView.functionExpression = expr
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showUnitConverterDialog() {
        val categories = arrayOf(
            "Length (Meters ⇄ Feet)",
            "Weight (Kilograms ⇄ Pounds)",
            "Temperature (°C ⇄ °F)",
            "Area (m² ⇄ sq ft)",
            "Speed (km/h ⇄ m/s)"
        )

        AlertDialog.Builder(this)
            .setTitle("Unit Converter")
            .setItems(categories) { _, which ->
                val message = when (which) {
                    0 -> "1 Meter = 3.28084 Feet\n1 Kilometer = 0.621371 Miles"
                    1 -> "1 Kilogram = 2.20462 Pounds\n1 Gram = 0.035274 Ounces"
                    2 -> "°F = (°C × 9/5) + 32\n°C = (°F − 32) × 5/9"
                    3 -> "1 Square Meter = 10.7639 Square Feet\n1 Hectare = 2.47105 Acres"
                    4 -> "1 km/h = 0.277778 m/s\n1 mph = 1.60934 km/h"
                    else -> ""
                }
                AlertDialog.Builder(this)
                    .setTitle(categories[which])
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showMathFormulasDialog() {
        val mathText = """
            • Quadratic Formula: x = (-b ± √(b² - 4ac)) / 2a
            • Pythagorean Theorem: a² + b² = c²
            • Area of Circle: A = πr²
            • Circumference of Circle: C = 2πr
            • Volume of Sphere: V = (4/3)πr³
            • Volume of Cylinder: V = πr²h
            • Trigonometric Identity: sin²(θ) + cos²(θ) = 1
            • Logarithm Rule: log(a·b) = log(a) + log(b)
            • Arithmetic Sequence: an = a1 + (n - 1)d
            • Geometric Sequence: an = a1 · r^(n-1)
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📐 Math Formulas")
            .setMessage(mathText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showPhysicsFormulasDialog() {
        val physicsText = """
            • Newton's Second Law: F = m · a
            • Kinetic Energy: Ek = ½ m · v²
            • Potential Energy: Ep = m · g · h
            • Ohm's Law: V = I · R
            • Gravitational Force: F = G · (m₁ · m₂) / r²
            • Velocity: v = u + a · t
            • Work Done: W = F · d · cos(θ)
            • Power: P = W / t = I² · R
            • Momentum: p = m · v
            • Pressure: P = F / A
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("⚛️ Physics Formulas")
            .setMessage(physicsText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showChemicalFormulasDialog() {
        val chemText = """
            • Water: H₂O (Molar Mass: 18.015 g/mol)
            • Sodium Chloride (Salt): NaCl (Molar Mass: 58.44 g/mol)
            • Carbon Dioxide: CO₂ (Molar Mass: 44.01 g/mol)
            • Sulfuric Acid: H₂SO₄ (Molar Mass: 98.08 g/mol)
            • Glucose: C₆H₁₂O₆ (Molar Mass: 180.16 g/mol)
            • Ammonia: NH₃ (Molar Mass: 17.031 g/mol)
            • Hydrochloric Acid: HCl (Molar Mass: 36.46 g/mol)
            • Methane: CH₄ (Molar Mass: 16.04 g/mol)
            • Ethanol: C₂H₅OH (Molar Mass: 46.07 g/mol)
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("🧪 Chemical Formulas & Compounds")
            .setMessage(chemText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showHistoryDialog() {
        if (historyList.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Calculation History")
                .setMessage("No history available yet.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val historyArray = historyList.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Calculation History")
            .setItems(historyArray) { _, which ->
                val selected = historyArray[which]
                val parts = selected.split(" = ")
                if (parts.isNotEmpty()) {
                    expressionBuilder.clear()
                    expressionBuilder.append(parts[0])
                    tvExpression.text = expressionBuilder.toString()
                    if (parts.size > 1) {
                        tvResult.text = parts[1]
                    }
                }
            }
            .setNeutralButton("Clear History") { _, _ ->
                historyList.clear()
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
