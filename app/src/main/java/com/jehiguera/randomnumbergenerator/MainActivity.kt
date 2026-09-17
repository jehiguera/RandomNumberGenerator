package com.jehiguera.randomnumbergenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.SecureRandom

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { RandomizerApp() } }
    }
}

private fun SecureRandom.nextLongBounded(bound: Long): Long {
    require(bound > 0)
    var bits: Long
    var value: Long
    do {
        bits = nextLong().ushr(1)
        value = bits % bound
    } while (bits - value + (bound - 1) < 0L)
    return value
}

private fun SecureRandom.nextIntInclusive(min: Int, max: Int): Int {
    val range = max.toLong() - min.toLong() + 1L
    return (min.toLong() + nextLongBounded(range)).toInt()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomGeneratorScreen() {
    var minText by remember { mutableStateOf("1") }
    var maxText by remember { mutableStateOf("100") }
    var countText by remember { mutableStateOf("6") }
    var unique by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val rng = remember { SecureRandom() }

    fun generate() {
        val min = minText.toIntOrNull()
        val max = maxText.toIntOrNull()
        val count = countText.toIntOrNull()
        error = when {
            min == null || max == null || count == null -> "Introduce valores numéricos válidos."
            min > max -> "El mínimo no puede ser mayor que el máximo."
            count < 1 -> "La cantidad debe ser al menos 1."
            count > 10000 -> "La cantidad máxima en esta versión es 10000."
            unique && count.toLong() > (max.toLong() - min.toLong() + 1L) -> "No hay suficientes números distintos en ese rango."
            else -> null
        }
        if (error != null) return

        val values = if (unique) {
            val set = linkedSetOf<Int>()
            while (set.size < count!!) set += rng.nextIntInclusive(min!!, max!!)
            set.toList()
        } else {
            List(count!!) { rng.nextIntInclusive(min!!, max!!) }
        }
        result = values.joinToString("  ·  ")
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Randomizer") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("v0.2.0", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(minText, { minText = it }, label = { Text("Mínimo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(maxText, { maxText = it }, label = { Text("Máximo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(countText, { countText = it }, label = { Text("Cantidad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = unique, onCheckedChange = { unique = it })
                Spacer(Modifier.width(12.dp))
                Text("Sin números repetidos")
            }
            Button(onClick = { generate() }, modifier = Modifier.fillMaxWidth()) { Text("GENERAR") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (result.isNotBlank()) {
                HorizontalDivider()
                Text("Resultado", style = MaterialTheme.typography.titleMedium)
                Text(result, fontSize = 28.sp)
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(result)) }, modifier = Modifier.fillMaxWidth()) { Text("COPIAR") }
            }
        }
    }
}

data class RandomTool(val emoji: String, val name: String, val kind: String)

private val randomTools = listOf(
    RandomTool("🔢", "Número al azar", "number"),
    RandomTool("🎲", "Dados", "dice"),
    RandomTool("🪙", "Moneda", "coin"),
    RandomTool("✅", "Sí o No", "yesno"),
    RandomTool("🎡", "Ruleta / Lista", "list"),
    RandomTool("✊", "Piedra, papel o tijera", "rps"),
    RandomTool("🎨", "Color al azar", "color"),
    RandomTool("🍾", "Girar botella", "bottle")
)

@Composable
fun RandomizerApp() {
    var tool by remember { mutableStateOf<RandomTool?>(null) }
    if (tool?.kind == "number") {
        Column {
            TextButton(onClick = { tool = null }) { Text("‹ MENÚ") }
            RandomGeneratorScreen()
        }
    } else if (tool != null) {
        RandomSimpleScreen(tool!!) { tool = null }
    } else {
        Scaffold { p ->
            LazyColumn(
                modifier = Modifier.padding(p).padding(18.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Randomizer", fontSize = 36.sp)
                    Text("Rápido, justo y divertido")
                    Text("v0.2.0", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                }
                items(randomTools) { t ->
                    ElevatedCard(onClick = { tool = t }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(t.emoji, fontSize = 34.sp)
                            Spacer(Modifier.width(16.dp))
                            Text(t.name, fontSize = 21.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomSimpleScreen(tool: RandomTool, back: () -> Unit) {
    var result by remember { mutableStateOf("—") }
    var options by remember { mutableStateOf("Ana\nLuis\nMarta\nCarlos") }
    val secure = remember { SecureRandom() }
    Scaffold(topBar = {
        TopAppBar(title = { Text(tool.name) }, navigationIcon = {
            TextButton(onClick = back) { Text("‹ MENÚ") }
        })
    }) { p ->
        Column(
            Modifier.padding(p).padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (tool.kind == "list") {
                OutlinedTextField(
                    value = options,
                    onValueChange = { options = it },
                    label = { Text("Una opción por línea") },
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
            Text(result, fontSize = 40.sp)
            Button(onClick = {
                result = when (tool.kind) {
                    "dice" -> "🎲 " + (secure.nextInt(6) + 1)
                    "coin" -> if (secure.nextBoolean()) "CARA" else "CRUZ"
                    "yesno" -> if (secure.nextBoolean()) "SÍ" else "NO"
                    "rps" -> listOf("PIEDRA ✊", "PAPEL ✋", "TIJERA ✌️")[secure.nextInt(3)]
                    "color" -> "#%06X".format(secure.nextInt(0x1000000))
                    "bottle" -> "Dirección: " + secure.nextInt(360) + "°"
                    "list" -> {
                        val list = options.lines().filter { it.isNotBlank() }
                        if (list.isEmpty()) "Añade opciones" else list[secure.nextInt(list.size)]
                    }
                    else -> "—"
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (tool.kind == "list") "GIRAR / ELEGIR" else "GENERAR")
            }
        }
    }
}
