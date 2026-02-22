package urlshorten.common

object Base62 {
    private const val BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private const val BASE = BASE62_CHARS.length

    fun encode(input: Long): String {
        if (input == 0L) return "0"

        val result = StringBuilder()
        var num = input

        while (num > 0) {
            result.insert(0, BASE62_CHARS[(num % BASE).toInt()])
            num /= BASE
        }

        return result.toString()
    }

    fun decode(input: String): Long {
        var result = 0L
        for (char in input) {
            result = result * BASE + BASE62_CHARS.indexOf(char)
        }
        return result
    }
}
