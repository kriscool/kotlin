package com.example

class Validator {

    fun validate(pass: String, pass2: String): ErrorsRegister {
        var error: ErrorsRegister

        if (pass.length > 4) {
            val chars = pass.toCharArray()
            var isUpper = false
            for (char in chars) {
                if (char.isUpperCase()) {
                    isUpper = true
                    break
                }
            }

            if (isUpper) {
                var haveSpecialSign = false
                val listOfSpecialSigns = listOf(
                    "!",
                    "@",
                    "#",
                    "$",
                    "%",
                    "^",
                    "&",
                    "*",
                    "(",
                    ")",
                    "_",
                    "-",
                    "+",
                    "=",
                    "[",
                    "{",
                    "]",
                    "}",
                    ":",
                    ";",
                    "\"",
                    "'",
                    ",",
                    ".",
                    "<",
                    ">",
                    "/",
                    "?",
                    "|"
                )
                for (char in chars) {
                    for (reg in listOfSpecialSigns) {
                        if (char.toString() == reg) {
                            haveSpecialSign = true
                            break
                        }
                    }
                }
                if (haveSpecialSign) {
                    if (pass == pass2) {
                        error = ErrorsRegister.OK
                    } else {
                        error = ErrorsRegister.PASSWORD_NOT_MATCH
                    }
                } else {
                    error = ErrorsRegister.NO_SPECIAL_SIGN
                }
            } else {
                error = ErrorsRegister.NO_CAPITAL_LETTER
            }
        } else {
            error = ErrorsRegister.PASS_TOO_SHORT
        }

        return error

    }

}

enum class ErrorsRegister {
    OK,
    PASSWORD_NOT_MATCH,
    NO_SPECIAL_SIGN,
    NO_CAPITAL_LETTER,
    PASS_TOO_SHORT
}
