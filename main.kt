import kotlin.math.max

fun main(args: Array<String>) {
    println("Welcome to hashmap-interface! Type 'help' to view a list of commands.")
    println("At any step, press ENTER without typing anything to abort the command")
    commandloop()
}


fun commandloop() {
    val data = mutableMapOf<String, String>()
    while (true) {
        print("@ ")
        var cmd = readLine() ?: break
        cmd = cmd.trim()
        if (cmd == "" || cmd == "exit") {
            break
        }
        when (cmd) {
            "add" -> commandadd(data)
            "remove" -> commandremove(data)
            "find" -> commandfind(data)
            "showall" -> {
                if (data.count() == 0) println("No entries")
                for ((key, value) in data) {
                    println("key: \"$key\"")
                    println("value: \"$value\"")
                    println()
                }
            }
            "help" -> printhelp()
            else -> {
                println("No such command: $cmd")
                println("To get a full list of commands type \"help\"")
            }
        }
    }
    println("Bye!")
}

fun printhelp() {
    println("   add          adds a new pair to the hashmap.\n" +
            "                if there is a pair already, overwrites it.")
    println("   remove       removes pair from hashmap, by key or by value")
    println("   find         finds pair by fragment of key or value")
    println("   help         prints list of commands")
}


class EntryHash (
    val keyhashes: Array<Int>,
    val valuehashes: Array<Int>
)

val entryhashes = mutableMapOf<String, EntryHash>()
const val k: Long = 239
const val m: Long = (1e9 + 7).toLong()

val kpows = mutableListOf(1)

fun commandadd(data: MutableMap<String, String>) {
    print("Type key: ")
    val key = readLine() ?: return
    if (key == "") return
    print("Type value: ")
    val value = readLine() ?: return
    if (value == "") return
    val alreadywas = data[key] != null
    data[key] = value

    val keyhashes = Array(key.count() + 1, {0})
    val valuehashes = Array(value.count() + 1, {0})

    for (i in 1..max(key.count(), value.count())) {
        if (i <= key.count()) {
            keyhashes[i] = ((keyhashes[i - 1].toLong() * k + key[i - 1].toLong()) % m).toInt()
        }
        if (i <= value.count()) {
            valuehashes[i] = ((valuehashes[i - 1].toLong() * k + value[i - 1].toLong()) % m).toInt()
        }
        if (i == kpows.count()) {
            kpows.add(((kpows[i - 1].toLong() * k) % m).toInt())
        }
    }

    entryhashes[key] = EntryHash(keyhashes, valuehashes)
    println("Entry ${if (alreadywas) "updated" else "added"}")
}

fun commandfind(data: MutableMap<String, String>) {
    val keys: List<String>
    if (yesno("Do you have a full key/value?") ?: return) {
        keys = findbyfull(data) ?: return
    } else {
        val findbykey = ask("Search for pair by key fragment or value fragment?",
                "key", "value") ?: return

        print("Enter ${if (findbykey) "key" else "value"} fragment: ")
        val input = readLine() ?: return
        if (input == "") return
        keys = mutableListOf<String>()
        for ((key, hashedentry) in entryhashes) {
            if (contains(input,
                            if (findbykey) key
                            else data[key]!!, // the keys are the same.
                            if (findbykey) hashedentry.keyhashes
                            else hashedentry.valuehashes)) {
                keys.add(key)
            }
        }
    }
    if (keys.count() == 0) {
        println("Sorry, no entries found")
    } else {
        println("These entries were found:")
        for (key in keys) {
            println("  key: \"$key\"")
            println("  value: \"${data[key]}\"")
            println()
        }
    }
}

fun contains(str:String, otherstring:String, hashes: Array<Int>): Boolean {
    if (str.count() >= hashes.count()) return false
    val strhash = hash(str)

    for (i in 0 until hashes.count() - str.count()) {
        var substrhash = ((hashes[i + str.count()].toLong()
                - hashes[i].toLong() * kpows[str.count()].toLong()) % m).toInt()
        if (substrhash < 0) substrhash += m.toInt()

        if (strhash == substrhash
                && str == otherstring.substring(i, i + str.count())) return true
    }
    return false
}

fun findbyfull(data: MutableMap<String, String>): List<String>? {
    val findbykey = ask("Find entry by key or value?",
            "key", "value") ?: return null

    val valid: (String) -> Boolean =
            if (findbykey) {{data.containsKey(it)}}
            else {{findbyvalue(data, it)}}
    print("Enter ${if (findbykey) "key" else "value"}: ")
    var input = readLine() ?: return null
    if (input == "") return null
    while (!valid(input)){
        if (yesno("Failed to find entries. Try again?") != true) return null
        print("Enter ${if (findbykey) "key" else "value"}: ")
        input = readLine() ?: return null
        if (input == "") return null
    }
    return if (findbykey) listOf(input) else getkeys()
}

fun commandremove(data: MutableMap<String, String>) {
    val keytodelete = selectkey(findbyfull(data)?: return)
    data.remove(keytodelete)
    entryhashes.remove(keytodelete)
    println("Entry removed from map")
}


fun ask (question:String, option1:String, option2:String) : Boolean? {
    print("$question [$option1/$option2]? ")
    var input = (readLine() ?: return null).toLowerCase()
    while (input !in listOf(option1, option2, "")) {
        println("Please type one of the options, or nothing and press enter.")
        print("$question [$option1/$option2]? ")
        input = (readLine() ?: return null).toLowerCase()
    }
    if (input == "") return null
    return input == option1
}

fun selectkey(keys: List<String>): String? {
    if (keys.count() == 1) return keys[0]
    println("More than one key available. Please choose one of these:")
    for (i in 0 until keys.count()) {
        println("$i) ${keys[i]}")
    }
    print(": ")
    var input = readLine() ?: return null
    while(input != "" &&
            (input.toIntOrNull() == null || 0 > input.toInt() || input.toInt() >= keys.count())) {
        println("Invalid input. Please type a number between 0 and ${keys.count() - 1}, " +
                "or nothing and press enter")
        print(": ")
        input = readLine() ?: return null
    }
    if (input == "") return null
    return keys[input.toInt()]
}

fun yesno(question:String) : Boolean? {
    return ask(question, "y", "n")
}

var keysfromvalue = mutableListOf<String>()

fun findbyvalue(data: MutableMap<String, String>, searchedvalue: String): Boolean {
    keysfromvalue.clear()
    val strhash = hash(searchedvalue)
    for ((key, entry) in entryhashes) {
        if (strhash == entry.valuehashes.last() &&
                searchedvalue == data[key]) keysfromvalue.add(key)
    }
    return keysfromvalue.count() != 0
}

fun hash(str:String): Int {
    var strhash = 0
    for (i in 0 until str.count()) {
        strhash = ((strhash.toLong() * k) % m).toInt()
        strhash += str[i].toInt()
    }
    return (strhash.toLong() % m).toInt()
}

fun getkeys(): List<String> {
    return keysfromvalue
}

