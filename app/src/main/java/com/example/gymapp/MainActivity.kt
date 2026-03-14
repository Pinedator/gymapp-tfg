package com.example.gymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.ui.theme.*
import com.example.gymapp.ui.theme.GymAppTheme

/* -------------------- DATA CLASSES -------------------- */

data class Ejercicio(val name: String, val sets: Int, val reps: Int, val weight: Float)
data class Rutina(val id: Int = 0, val name: String, val ejercicios: List<Ejercicio>)
data class Stats(val strength: Int, val consistency: Int)
data class User(val name: String, val level: Int, val xp: Int, val stats: Stats)
data class Historial(val routineName: String, val exercisesDone: List<Ejercicio>, val xpGained: Int)

/* -------------------- LOGICA -------------------- */

fun calculateStrengthGain(ejercicio: Ejercicio): Int =
    ((ejercicio.sets * ejercicio.reps * ejercicio.weight) / 300).toInt()

/* -------------------- COMPONENTES COMUNES -------------------- */

@Composable
fun GamingBackground(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = NeonPurple.copy(alpha = 0.04f), radius = size.width * 0.7f,
                center = Offset(size.width * 0.85f, size.height * 0.1f))
            drawCircle(color = NeonCyan.copy(alpha = 0.03f), radius = size.width * 0.5f,
                center = Offset(size.width * 0.1f, size.height * 0.7f))
        }
        content()
    }
}

@Composable
fun GamingCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, Brush.horizontalGradient(listOf(NeonPurple.copy(0.3f), NeonCyan.copy(0.2f))), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun GamingTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    TextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = TextMuted, fontSize = 12.sp) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = DarkCard,
            unfocusedContainerColor = DarkCard,
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            focusedIndicatorColor   = NeonCyan,
            unfocusedIndicatorColor = TextMuted.copy(0.4f),
            cursorColor             = NeonCyan,
            focusedLabelColor       = NeonCyan,
        ),
        modifier = modifier
    )
}

/* -------------------- AVATAR RPG -------------------- */

@Composable
fun RpgAvatar(level: Int, modifier: Modifier = Modifier) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val borderColor = when { level >= 10 -> NeonGold; level >= 5 -> NeonPurple; else -> NeonCyan }

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(110.dp)) {
        Canvas(modifier = Modifier.size(110.dp)) {
            drawCircle(color = borderColor.copy(alpha = glowAlpha * 0.3f), radius = size.minDimension / 2f)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(90.dp)
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                .background(Brush.radialGradient(listOf(DarkCard, DarkSurface)), CircleShape)
                .border(2.dp, Brush.sweepGradient(listOf(borderColor, NeonPurple, borderColor)), CircleShape)
        ) {
            val emoji = when { level >= 10 -> "👑"; level >= 5 -> "⚔️"; else -> "🥊" }
            Text(emoji, fontSize = 36.sp, textAlign = TextAlign.Center)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.BottomEnd).size(28.dp)
                .background(borderColor, CircleShape).border(1.dp, DarkBg, CircleShape)
        ) {
            Text("$level", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun XpProgressBar(xp: Int, maxXp: Int = 100, modifier: Modifier = Modifier) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(xp) { animProgress.animateTo(xp / maxXp.toFloat(), tween(800)) }
    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("XP", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("$xp / $maxXp", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(DarkCard)) {
            Box(modifier = Modifier.fillMaxWidth(animProgress.value).fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(listOf(NeonPurple, NeonCyan))))
        }
    }
}

@Composable
fun StatBar(label: String, value: Int, maxValue: Int = 100, color: Color, modifier: Modifier = Modifier) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(value) { animProgress.animateTo((value / maxValue.toFloat()).coerceIn(0f, 1f), tween(600)) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(110.dp))
        Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(DarkCard)) {
            Box(modifier = Modifier.fillMaxWidth(animProgress.value).fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(Brush.horizontalGradient(listOf(color.copy(0.6f), color))))
        }
        Spacer(Modifier.width(8.dp))
        Text("$value", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
    }
}

/* -------------------- DASHBOARD -------------------- */

@Composable
fun UserDashboard(user: User, onChangeName: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(user.name) }
    val rankTitle = when { user.level >= 10 -> "LEYENDA"; user.level >= 5 -> "GUERRERO"; else -> "NOVATO" }
    val rankColor = when { user.level >= 10 -> NeonGold; user.level >= 5 -> NeonPurple; else -> NeonCyan }

    GamingBackground {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(12.dp))
            Text("PERFIL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Spacer(Modifier.height(20.dp))
            RpgAvatar(level = user.level)
            Spacer(Modifier.height(12.dp))
            Text(rankTitle, color = rankColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
            Spacer(Modifier.height(6.dp))

            if (editing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GamingTextField(value = newName, onValueChange = { newName = it }, label = "Nombre", modifier = Modifier.width(180.dp))
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onChangeName(newName); editing = false }) {
                        Text("✓", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { editing = false; newName = user.name }) {
                        Text("✕", color = TextMuted, fontSize = 16.sp)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { editing = true; newName = user.name }) {
                        Text("✏", color = TextMuted, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            GamingCard {
                XpProgressBar(xp = user.xp, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = TextMuted.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))
                StatBar("⚔ FUERZA", user.stats.strength, color = NeonPurple)
                Spacer(Modifier.height(10.dp))
                StatBar("🔥 CONSTANCIA", user.stats.consistency, color = NeonGold)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(DarkCard)
                    .border(1.dp, NeonCyan.copy(0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NIVEL ", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("${user.level}", color = NeonCyan, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/* -------------------- RUTINAS -------------------- */

@Composable
fun RoutineList(
    rutinas: SnapshotStateList<Rutina>,
    onCompleteRoutine: (Rutina) -> Unit,
    onEditRoutine: (Rutina) -> Unit,
    onDeleteRoutine: (Rutina) -> Unit
) {
    var rutinaToDelete by remember { mutableStateOf<Rutina?>(null) }

    GamingBackground {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("RUTINAS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))

            if (rutinas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💪", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Sin rutinas todavía", color = TextMuted, fontSize = 14.sp)
                        Text("Pulsa + para crear una", color = TextMuted.copy(0.6f), fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(rutinas) { _, routine ->
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface)
                                .border(1.dp, Brush.horizontalGradient(listOf(NeonPurple.copy(0.3f), NeonCyan.copy(0.2f))), RoundedCornerShape(16.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onEditRoutine(routine) }, onLongPress = { rutinaToDelete = routine })
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(NeonCyan, CircleShape))
                                    Spacer(Modifier.width(8.dp))
                                    Text(routine.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(10.dp))
                                routine.ejercicios.forEach { ex ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(ex.name, color = TextMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                        Text("${ex.sets}x${ex.reps}  ${ex.weight}kg",
                                            color = NeonCyan.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { onCompleteRoutine(routine) },
                                    colors  = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    border  = androidx.compose.foundation.BorderStroke(1.dp, Brush.horizontalGradient(listOf(NeonPurple, NeonCyan))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚡ COMPLETAR", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (rutinaToDelete != null) {
        AlertDialog(
            onDismissRequest  = { rutinaToDelete = null },
            containerColor    = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor  = TextMuted,
            title = { Text("Eliminar rutina", fontWeight = FontWeight.Bold) },
            text  = { Text("¿Seguro que quieres eliminar \"${rutinaToDelete?.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onDeleteRoutine(rutinaToDelete!!); rutinaToDelete = null }) {
                    Text("ELIMINAR", color = Color(0xFFFF4444), fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rutinaToDelete = null }) { Text("Cancelar", color = TextMuted) }
            }
        )
    }
}

/* -------------------- AÑADIR / EDITAR RUTINA -------------------- */

@Composable
fun AddRoutineScreen(rutina: Rutina? = null, onAddRoutine: (Rutina) -> Unit, onCancel: () -> Unit) {
    var routineName by remember { mutableStateOf(rutina?.name ?: "") }
    val ejercicios = remember { mutableStateListOf<Ejercicio>().apply { rutina?.ejercicios?.let { addAll(it) } } }

    GamingBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(if (rutina != null) "EDITAR RUTINA" else "NUEVA RUTINA",
                    color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                GamingTextField(value = routineName, onValueChange = { routineName = it },
                    label = "Nombre de la rutina", modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            itemsIndexed(ejercicios) { index, exercise ->
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, NeonPurple.copy(0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(NeonPurple, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("EJERCICIO ${index + 1}", color = NeonPurple, fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { ejercicios.removeAt(index) }) {
                                Text("✕", color = Color(0xFFFF4444), fontSize = 14.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        GamingTextField(value = exercise.name,
                            onValueChange = { ejercicios[index] = ejercicios[index].copy(name = it) },
                            label = "Nombre del ejercicio", modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GamingTextField(value = exercise.sets.toString(),
                                onValueChange = { ejercicios[index] = ejercicios[index].copy(sets = it.toIntOrNull() ?: exercise.sets) },
                                label = "Sets", modifier = Modifier.weight(1f))
                            GamingTextField(value = exercise.reps.toString(),
                                onValueChange = { ejercicios[index] = ejercicios[index].copy(reps = it.toIntOrNull() ?: exercise.reps) },
                                label = "Reps", modifier = Modifier.weight(1f))
                            GamingTextField(value = exercise.weight.toString(),
                                onValueChange = { ejercicios[index] = ejercicios[index].copy(weight = it.toFloatOrNull() ?: exercise.weight) },
                                label = "Kg", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick  = { ejercicios.add(Ejercicio("", 0, 0, 0f)) },
                    border   = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(0.5f)),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ AÑADIR EJERCICIO", color = NeonPurple, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (routineName.isNotBlank() && ejercicios.isNotEmpty())
                            onAddRoutine(Rutina(id = rutina?.id ?: 0, name = routineName, ejercicios = ejercicios.toList()))
                    },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border  = androidx.compose.foundation.BorderStroke(1.dp, Brush.horizontalGradient(listOf(NeonPurple, NeonCyan))),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    Text("💾 GUARDAR RUTINA", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar", color = TextMuted, fontSize = 13.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/* -------------------- HISTORIAL -------------------- */

@Composable
fun HistoryList(history: List<Historial>) {
    GamingBackground {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("HISTORIAL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📜", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Sin actividad todavía", color = TextMuted, fontSize = 14.sp)
                        Text("Completa una rutina para empezar", color = TextMuted.copy(0.6f), fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(history.reversed()) { _, item ->
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface)
                                .border(1.dp, Brush.horizontalGradient(listOf(NeonGold.copy(0.3f), NeonPurple.copy(0.2f))), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(NeonGold, CircleShape))
                                        Spacer(Modifier.width(8.dp))
                                        Text(item.routineName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(NeonGold.copy(0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)) {
                                        Text("+${item.xpGained} XP", color = NeonGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                item.exercisesDone.forEach { ex ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(ex.name, color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("${ex.sets}x${ex.reps}  ${ex.weight}kg",
                                            color = NeonGold.copy(0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

/* -------------------- APP -------------------- */

@Composable
fun GymApp() {
    val context = LocalContext.current
    val db = remember { DatabaseHelper(context) }

    var user by remember { mutableStateOf(db.getUser()) }
    var rutinaToEdit by remember { mutableStateOf<Rutina?>(null) }
    val rutinas = remember { mutableStateListOf<Rutina>().apply { addAll(db.getAllRoutines()) } }
    var history by remember { mutableStateOf(db.getAllHistory()) }
    var showAddRoutine by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }

// 2. Cuando estás en el menú principal → pregunta antes de salir
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showAddRoutine || rutinaToEdit != null -> {
                showAddRoutine = false
                rutinaToEdit = null
            }
            else -> showExitDialog = true
        }
    }

    fun completeRoutine(rutina: Rutina) {
        var xpGained = 0; var strengthGain = 0
        rutina.ejercicios.forEach { strengthGain += calculateStrengthGain(it); xpGained += (it.sets * it.reps * it.weight / 300).toInt() }
        var newLevel = user.level; var xp = user.xp + xpGained
        while (xp >= 100) { xp -= 100; newLevel++ }
        user = user.copy(level = newLevel, xp = xp, stats = Stats(user.stats.strength + strengthGain, user.stats.consistency + 1))
        db.updateUser(user)
        val record = Historial(rutina.name, rutina.ejercicios, xpGained)
        db.insertHistory(record)
        history = history + record
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest  = { showExitDialog = false },
            containerColor    = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor  = TextMuted,
            title = { Text("¿Salir?", fontWeight = FontWeight.Bold) },
            text  = { Text("¿Seguro que quieres salir de la app?") },
            confirmButton = {
                TextButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                    Text("SALIR", color = Color(0xFFFF4444), fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            if (!showAddRoutine && rutinaToEdit == null && tab == 1) {
                FloatingActionButton(
                    onClick        = { showAddRoutine = true },
                    containerColor = DarkCard,
                    shape          = CircleShape,
                    modifier       = Modifier.border(1.dp, Brush.sweepGradient(listOf(NeonPurple, NeonCyan, NeonPurple)), CircleShape)
                ) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (showAddRoutine || rutinaToEdit != null) {
                AddRoutineScreen(
                    rutina = rutinaToEdit,
                    onAddRoutine = { newRoutine ->
                        if (rutinaToEdit != null) {
                            db.updateRoutine(newRoutine)
                            val index = rutinas.indexOfFirst { it.id == newRoutine.id }
                            if (index >= 0) rutinas[index] = newRoutine
                            rutinaToEdit = null
                        } else {
                            val newId = db.insertRoutine(newRoutine)
                            rutinas.add(newRoutine.copy(id = newId.toInt()))
                        }
                        showAddRoutine = false
                    },
                    onCancel = { showAddRoutine = false; rutinaToEdit = null }
                )
            } else {
                Column {
                    // NavigationBar gaming
                    Box(
                        modifier = Modifier.fillMaxWidth().background(DarkSurface)
                            .border(width = 1.dp,
                                brush = Brush.horizontalGradient(listOf(NeonPurple.copy(0.3f), NeonCyan.copy(0.3f))),
                                shape = RoundedCornerShape(0.dp))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(Triple(0, "👤", "PERFIL"), Triple(1, "💪", "RUTINAS"), Triple(2, "📜", "HISTORIAL"))
                                .forEach { (index, icon, label) ->
                                    val selected = tab == index
                                    TextButton(onClick = { tab = index }, modifier = Modifier.weight(1f)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(icon, fontSize = 18.sp)
                                            Text(label,
                                                color = if (selected) NeonCyan else TextMuted,
                                                fontSize = 9.sp,
                                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                                                letterSpacing = 1.sp)
                                            if (selected) {
                                                Box(modifier = Modifier.width(20.dp).height(2.dp)
                                                    .background(Brush.horizontalGradient(listOf(NeonPurple, NeonCyan)), RoundedCornerShape(1.dp)))
                                            }
                                        }
                                    }
                                }
                        }
                    }

                    when (tab) {
                        0 -> UserDashboard(user, onChangeName = { user = user.copy(name = it); db.updateUser(user) })
                        1 -> RoutineList(rutinas,
                            onCompleteRoutine = { completeRoutine(it) },
                            onEditRoutine     = { rutinaToEdit = it },
                            onDeleteRoutine   = { db.deleteRoutine(it.id); rutinas.remove(it) })
                        2 -> HistoryList(history)
                    }
                }
            }
        }
    }
}

/* -------------------- ACTIVITY -------------------- */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GymAppTheme { GymApp() } }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    GymAppTheme { GymApp() }
}