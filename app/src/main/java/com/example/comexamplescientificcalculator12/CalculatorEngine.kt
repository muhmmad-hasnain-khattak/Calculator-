package com.example.comexamplescientificcalculator12

import kotlin.math.*

class CalculatorEngine(var isRadians: Boolean = false) {

    fun evaluate(expression: String): Double {
        val sanitized = preprocess(expression)
        val tokens = tokenize(sanitized)
        val parser = Parser(tokens, isRadians)
        return parser.parse()
    }

    private fun preprocess(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("x²", "^2")
            .replace("x³", "^3")
            .replace("1/x", "inv")
            .replace("e^x", "exp")
            .replace("10^x", "pow10")
            .replace("mod", "%")
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(Token(TokenType.NUMBER, sb.toString()))
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isLetter() || expr[i].isDigit() || expr[i] == '_')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(Token(TokenType.FUNCTION, sb.toString().lowercase()))
                }
                c in "+-*/^%!()," -> {
                    tokens.add(Token(TokenType.OPERATOR, c.toString()))
                    i++
                }
                else -> {
                    tokens.add(Token(TokenType.OPERATOR, c.toString()))
                    i++
                }
            }
        }
        return tokens
    }

    enum class TokenType { NUMBER, FUNCTION, OPERATOR }
    data class Token(val type: TokenType, val value: String)

    class Parser(private val tokens: List<Token>, private val isRadians: Boolean) {
        private var pos = 0

        private fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): Token? = if (pos < tokens.size) tokens[pos++] else null

        fun parse(): Double {
            if (tokens.isEmpty()) return 0.0
            return parseExpression()
        }

        private fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                val token = peek()
                if (token != null && token.type == TokenType.OPERATOR) {
                    if (token.value == "+") {
                        consume()
                        value += parseTerm()
                    } else if (token.value == "-") {
                        consume()
                        value -= parseTerm()
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (true) {
                val token = peek()
                if (token != null && token.type == TokenType.OPERATOR) {
                    if (token.value == "*") {
                        consume()
                        value *= parseFactor()
                    } else if (token.value == "/") {
                        consume()
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        value /= divisor
                    } else if (token.value == "%") {
                        consume()
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Modulo by zero")
                        value %= divisor
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            return value
        }

        private fun parseFactor(): Double {
            var value = parseUnary()
            while (true) {
                val token = peek()
                if (token != null && token.type == TokenType.OPERATOR && token.value == "^") {
                    consume()
                    val exponent = parseUnary()
                    value = value.pow(exponent)
                } else {
                    break
                }
            }
            return value
        }

        private fun parseUnary(): Double {
            val token = peek()
            if (token != null && token.type == TokenType.OPERATOR) {
                if (token.value == "-") {
                    consume()
                    return -parseUnary()
                } else if (token.value == "+") {
                    consume()
                    return parseUnary()
                }
            }
            return parsePrimary()
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            when (token.type) {
                TokenType.NUMBER -> {
                    consume()
                    return token.value.toDoubleOrNull() ?: 0.0
                }
                TokenType.FUNCTION -> {
                    consume()
                    val funcName = token.value
                    var arg = 0.0
                    if (peek()?.value == "(") {
                        consume()
                        arg = parseExpression()
                        if (peek()?.value == ")") {
                            consume()
                        }
                    } else {
                        arg = parsePrimary()
                    }
                    return applyFunction(funcName, arg)
                }
                TokenType.OPERATOR -> {
                    if (token.value == "(") {
                        consume()
                        val value = parseExpression()
                        if (peek()?.value == ")") {
                            consume()
                        }
                        return parsePostfix(value)
                    } else {
                        throw IllegalArgumentException("Unexpected operator: ${token.value}")
                    }
                }
            }
        }

        private fun parsePostfix(value: Double): Double {
            val token = peek()
            if (token != null && token.type == TokenType.OPERATOR && token.value == "!") {
                consume()
                return factorial(value)
            }
            return value
        }

        private fun applyFunction(name: String, arg: Double): Double {
            val radArg = if (isRadians) arg else Math.toRadians(arg)
            return when (name) {
                "sin" -> sin(radArg)
                "cos" -> cos(radArg)
                "tan" -> tan(radArg)
                "asin" -> {
                    val res = asin(arg)
                    if (isRadians) res else Math.toDegrees(res)
                }
                "acos" -> {
                    val res = acos(arg)
                    if (isRadians) res else Math.toDegrees(res)
                }
                "atan" -> {
                    val res = atan(arg)
                    if (isRadians) res else Math.toDegrees(res)
                }
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "log" -> log10(arg)
                "ln" -> ln(arg)
                "sqrt" -> {
                    if (arg < 0) throw ArithmeticException("Square root of negative number")
                    sqrt(arg)
                }
                "cbrt" -> cbrt(arg)
                "abs" -> abs(arg)
                "inv" -> {
                    if (arg == 0.0) throw ArithmeticException("Division by zero")
                    1.0 / arg
                }
                "exp" -> exp(arg)
                "pow10" -> 10.0.pow(arg)
                "fact", "factorial" -> factorial(arg)
                else -> throw IllegalArgumentException("Unknown function: $name")
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n)) throw IllegalArgumentException("Factorial requires non-negative integer")
            if (n > 170) return Double.POSITIVE_INFINITY
            var result = 1.0
            for (i in 1..n.toInt()) {
                result *= i.toDouble()
            }
            return result
        }
    }
}
