package com.example.horamexico
import android.os.Bundle


import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // <- MODIFICACIÓN: Import necesario
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch // <- MODIFICACIÓN: Import necesario
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // El tema personalizado fue eliminado en tu código anterior,
            // lo mantengo simple con MaterialTheme.
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HoraMexicoScreen()
                }
            }
        }
    }
}

// Data classes para el parsing del JSON
data class WorldTimeResponse(
    val abbreviation: String,
    val client_ip: String,
    val datetime: String,
    val day_of_week: Int,
    val day_of_year: Int,
    val dst: Boolean,
    val dst_from: String?,
    val dst_offset: Int,
    val dst_until: String?,
    val raw_offset: Int,
    val timezone: String,
    val unixtime: Long,
    val utc_datetime: String,
    val utc_offset: String,
    val week_number: Int
)

// Interface para la API
interface WorldTimeApiService {
    @GET("America/Mexico_City")
    suspend fun getMexicoCityTime(): WorldTimeResponse
}

// Cliente Retrofit
object WorldTimeClient {
    private const val BASE_URL = "https://worldtimeapi.org/api/timezone/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: WorldTimeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(WorldTimeApiService::class.java)
    }
}

// ViewModel simplificado (usando composables)
class TimeViewModel {
    var currentTime by mutableStateOf<WorldTimeResponse?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    suspend fun fetchMexicoTime() {
        isLoading = true
        errorMessage = null
        try {
            currentTime = WorldTimeClient.instance.getMexicoCityTime()
        } catch (e: Exception) {
            errorMessage = when (e) {
                is java.net.UnknownHostException -> "Error de conexión. Verifica tu internet."
                is java.net.SocketTimeoutException -> "Tiempo de espera agotado."
                is retrofit2.HttpException -> "Error del servidor: ${e.code()}"
                else -> "Error: ${e.localizedMessage ?: "Desconocido"}"
            }
        } finally {
            isLoading = false
        }
    }

    fun formatTime(datetime: String): String {
        return try {
            val timePart = datetime.substring(11, 16) // Extrae HH:MM
            "$timePart hrs"
        } catch (e: Exception) {
            datetime
        }
    }

    fun formatDate(datetime: String): String {
        return try {
            val datePart = datetime.substring(0, 10)
            val parts = datePart.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            datetime
        }
    }

    fun getDayOfWeek(dayNumber: Int): String {
        // La API devuelve 1 para Lunes, 7 para Domingo. Si la API devolviera 0 para Domingo, habría que ajustar.
        return when (dayNumber) {
            1 -> "Lunes"
            2 -> "Martes"
            3 -> "Miércoles"
            4 -> "Jueves"
            5 -> "Viernes"
            6 -> "Sábado"
            7 -> "Domingo"
            0 -> "Domingo" // Por si acaso la API cambia el índice a base 0
            else -> "Día Desconocido"
        }
    }
}

@Composable
fun HoraMexicoScreen() {
    val viewModel = remember { TimeViewModel() }
    var autoRefresh by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope() // <- MODIFICACIÓN: Creamos el scope aquí

    // Cargar datos iniciales
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchMexicoTime()
    }

    // Auto-refresh cada 30 segundos
    LaunchedEffect(key1 = autoRefresh) {
        if (autoRefresh) {
            while (true) {
                delay(30000) // 30 segundos
                viewModel.fetchMexicoTime()
            }
        }
    }

    // Gradiente de fondo inspirado en la bandera de México
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF006847), // Verde
            Color(0xFFFFFFFF), // Blanco
            Color(0xFFCE1126)  // Rojo
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "Hora Oficial",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Centro de México",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Tarjeta principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        viewModel.isLoading && viewModel.currentTime == null -> {
                            LoadingState()
                        }

                        viewModel.errorMessage != null -> {
                            ErrorState(
                                errorMessage = viewModel.errorMessage!!,
                                // MODIFICACIÓN: Lanzamos la coroutine desde aquí
                                onRetry = {
                                    scope.launch {
                                        viewModel.fetchMexicoTime()
                                    }
                                }
                            )
                        }

                        viewModel.currentTime != null -> {
                            SuccessState(
                                timeData = viewModel.currentTime!!,
                                viewModel = viewModel,
                                // MODIFICACIÓN: También se necesita aquí para el refresh manual dentro del success
                                onRefresh = {
                                    scope.launch {
                                        viewModel.fetchMexicoTime()
                                    }
                                }
                            )
                        }

                        else -> {
                            LoadingState()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Controles adicionales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        // MODIFICACIÓN: Lanzamos la coroutine desde aquí también
                        scope.launch {
                            viewModel.fetchMexicoTime()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF006847)
                    ),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF006847)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar")
                }

                Button(
                    onClick = { autoRefresh = !autoRefresh },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (autoRefresh) Color(0xFF006847) else Color(0xFF666666),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Auto-actualizar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (autoRefresh) "Auto: ON" else "Auto: OFF")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Información adicional
            Text(
                text = "Fuente: WorldTimeAPI • Zona: America/Mexico_City",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(40.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = Color(0xFF006847),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Obteniendo hora oficial...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(60.dp),
            tint = Color(0xFFCE1126)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error de Conexión",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry, // onRetry ahora es seguro de llamar
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF006847),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reintentar",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
fun SuccessState(
    timeData: WorldTimeResponse,
    viewModel: TimeViewModel,
    onRefresh: () -> Unit // onRefresh también debe ser llamado desde un scope
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hora principal
        Text(
            text = viewModel.formatTime(timeData.datetime),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006847),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fecha
        Text(
            text = viewModel.formatDate(timeData.datetime),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Día de la semana
        Text(
            text = viewModel.getDayOfWeek(timeData.day_of_week),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Información detallada
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                InfoRow("Zona Horaria:", timeData.timezone)
                InfoRow("UTC Offset:", timeData.utc_offset)
                InfoRow("Abreviatura:", timeData.abbreviation)
                InfoRow("Horario de Verano:", if (timeData.dst) "Sí" else "No")
                InfoRow("Semana del año:", timeData.week_number.toString())
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HoraMexicoPreview() {
    MaterialTheme {
        HoraMexicoScreen()
    }
}
