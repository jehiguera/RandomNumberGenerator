package com.jehiguera.randomnumbergenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
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
    RandomTool("🍾", "Girar botella", "bottle"),
    RandomTool("🐾", "Equipos al azar", "teams")
)

@Composable
fun RandomizerApp() {
    var tool by remember { mutableStateOf<RandomTool?>(null) }
    if (tool?.kind == "number") {
        Column { TextButton(onClick = { tool = null }) { Text("‹ MENÚ") }; RandomGeneratorScreen() }
    } else if (tool != null) {
        when (tool!!.kind) {
            "dice" -> CatDiceScreen { tool = null }
            "list" -> CatRouletteScreen { tool = null }
            "teams" -> CatTeamsScreen { tool = null }
            else -> RandomSimpleScreen(tool!!) { tool = null }
        }
    } else {
        CatHomeScreen { tool = it }
    }
}

@Composable
fun CatHomeScreen(open: (RandomTool) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)) { p ->
        Column(Modifier.padding(p).padding(horizontal = 16.dp).fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Randomizer", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text("Rápido, justo y gatunamente divertido 😸", style = MaterialTheme.typography.bodyMedium)
                    Text("v0.2.1-dev · CAT UI", style = MaterialTheme.typography.labelMedium)
                }
                Text("🐱", fontSize = 54.sp)
            }
            Text("🐾  🧶  Elige un juego  🧶  🐾", modifier = Modifier.padding(vertical = 12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                gridItems(randomTools) { t ->
                    ElevatedCard(onClick = { open(t) }, modifier = Modifier.height(142.dp)) {
                        Column(Modifier.padding(14.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(catIcon(t.kind), fontSize = 38.sp)
                            Column {
                                Text(t.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text(catSubtitle(t.kind), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun catIcon(kind: String) = when(kind) {
    "dice" -> "🐱🎲"; "list" -> "🐱🎡"; "coin" -> "🐱🪙"; "yesno" -> "🐾❓"
    "rps" -> "😼✊"; "color" -> "🐱🎨"; "bottle" -> "🐈🍾"; "teams" -> "🐱🧶"; else -> "🐱🔢"
}
private fun catSubtitle(kind: String) = when(kind) {
    "dice" -> "El gato lanza hasta 20 dados"
    "list" -> "Gira la ruleta con tu minino"
    "coin" -> "Atrapa cara o cruz"
    "yesno" -> "Decisión felina instantánea"
    "rps" -> "Desafía al gato"
    "color" -> "Pinta con sus patitas"
    "bottle" -> "Persigue la botella"
    "teams" -> "Reparte ovillos y equipos"
    else -> "Números con suerte gatuna"
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatDiceScreen(back: () -> Unit) {
    var count by remember { mutableIntStateOf(2) }
    var dice by remember { mutableStateOf(listOf(1, 1)) }
    var turns by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(turns, tween(650), label = "dice")
    val rng = remember { SecureRandom() }
    Scaffold(topBar = { TopAppBar(title={Text("🐱 Dados juguetones")}, navigationIcon={TextButton(onClick=back){Text("‹ MENÚ")}}) }) { p ->
        Column(Modifier.padding(p).padding(20.dp).fillMaxSize(), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(18.dp)) {
            Text("🐱  🧶  🐾", fontSize=38.sp)
            Text("¿Cuántos dados?", style=MaterialTheme.typography.titleLarge)
            Row(verticalAlignment=Alignment.CenterVertically) {
                Button(onClick={ if(count>1) count-- }) { Text("−") }
                Text("  " + count + "  ", fontSize=30.sp)
                Button(onClick={ if(count<20) count++ }) { Text("+") }
            }
            Text(dice.joinToString("  ") { dieFace(it) }, fontSize=48.sp, modifier=Modifier.rotate(rotation))
            Text("Total: " + dice.sum(), fontSize=28.sp)
            Button(onClick={ dice=List(count){rng.nextInt(6)+1}; turns += 360f }, modifier=Modifier.fillMaxWidth()) { Text("🎲 ¡LANZAR! 🐾") }
            Text("El gatito promete no empujar los dados de la mesa 😸")
        }
    }
}
private fun dieFace(n:Int)=listOf("⚀","⚁","⚂","⚃","⚄","⚅")[n-1]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatRouletteScreen(back: () -> Unit) {
    var options by remember { mutableStateOf("Pizza\nPeli\nPaseo\nLeer\nVideojuego\nCocinar") }
    var result by remember { mutableStateOf("🐾") }
    var angle by remember { mutableFloatStateOf(0f) }
    val animated by animateFloatAsState(angle, tween(1500, easing=FastOutSlowInEasing), label="roulette")
    val rng=remember { SecureRandom() }
    Scaffold(topBar={TopAppBar(title={Text("🐱 Ruleta")},navigationIcon={TextButton(onClick=back){Text("‹ MENÚ")}})}) { p ->
        Column(Modifier.padding(p).padding(20.dp).fillMaxSize(), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("🐱", fontSize=58.sp)
            Box(Modifier.size(190.dp).rotate(animated).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(95.dp)), contentAlignment=Alignment.Center) { Text("🎡\n🧶  🐾  🧶", fontSize=32.sp) }
            OutlinedTextField(options,{options=it},label={Text("Opciones · una por línea")},modifier=Modifier.fillMaxWidth().height(150.dp))
            Button(onClick={ val list=options.lines().filter{it.isNotBlank()}; if(list.isNotEmpty()){ result=list[rng.nextInt(list.size)]; angle += 1080f+rng.nextInt(360) } },modifier=Modifier.fillMaxWidth()){Text("🎡 ¡GIRAR! 🐾")}
            Text("Resultado: " + result,fontSize=26.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatTeamsScreen(back: () -> Unit) {
    var names by remember { mutableStateOf("Ana\nLuis\nMarta\nCarlos\nSofía\nPedro") }
    var teams by remember { mutableIntStateOf(2) }
    var result by remember { mutableStateOf("") }
    val rng=remember { SecureRandom() }
    Scaffold(topBar={TopAppBar(title={Text("🐱 Equipos al azar")},navigationIcon={TextButton(onClick=back){Text("‹ MENÚ")}})}) { p ->
        Column(Modifier.padding(p).padding(20.dp).fillMaxSize(),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text("🐱🐱  Gatitos capitanes buscan equipo  🧶",fontSize=20.sp)
            OutlinedTextField(names,{names=it},label={Text("Participantes · uno por línea")},modifier=Modifier.fillMaxWidth().height(170.dp))
            Row(verticalAlignment=Alignment.CenterVertically){Text("Equipos: ",fontSize=20.sp); Button(onClick={if(teams>2)teams--}){Text("−")}; Text("  " + teams + "  ",fontSize=24.sp); Button(onClick={if(teams<10)teams++}){Text("+")}}
            Button(onClick={
                val pool=names.lines().filter{it.isNotBlank()}.toMutableList()
                for(i in pool.lastIndex downTo 1){val j=rng.nextInt(i+1); val x=pool[i];pool[i]=pool[j];pool[j]=x}
                result=(0 until teams).joinToString("\n\n"){t->"🐾 Equipo "+(t+1)+": "+pool.filterIndexed{i,_->i%teams==t}.joinToString(", ")}
            },modifier=Modifier.fillMaxWidth()){Text("🐾 CREAR EQUIPOS")}
            Text(result,fontSize=19.sp)
        }
    }
}
