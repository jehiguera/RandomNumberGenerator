package com.jehiguera.randomnumbergenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
        setContent { MaterialTheme { RandomGeneratorScreen() } }
    }
}

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
            unique && count > (max.toLong() - min.toLong() + 1L) -> "No hay suficientes números distintos en ese rango."
            else -> null
        }
        if (error != null) return

        val values = if (unique) {
            val set = linkedSetOf<Int>()
            while (set.size < count!!) set += rng.nextInt(max!! - min!! + 1) + min
            set.toList()
        } else {
            List(count!!) { rng.nextInt(max!! - min!! + 1) + min }
        }
        result = values.joinToString("  ·  ")
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Random Number Generator") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("v0.1.0", style = MaterialTheme.typography.labelMedium)
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
