
Aplicación 
de Consulta de Hora para MéxicoEste repositorio 
contiene el código fuente de una aplicación nativa
para Android diseñada para mostrar la hora actual 
centro de México. La aplicación ha sido desarrollada
utilizando las tecnologías más modernas recomendadas
para el ecosistema Android, incluyendo Kotlin y
Jetpack Compose.Resumen del ProyectoEl objetivo 
principal de la aplicación es consultar en tiempo real
la hora de la zona horaria America/Mexico_City a través 
de un servicio web externo y presentarla al usuario 
en una interfaz clara, robusta y reactiva. La aplicación
maneja diferentes estados, como la carga de datos, la 
visualización exitosa de la información y la gestión de 
posibles errores de conexión.Arquitectura y Componentes 
TécnicosLa aplicación sigue una arquitectura simplificada
pero modular, separando responsabilidades clave como la capa
de acceso a datos, la lógica de negocio y la interfaz de 
usuario.1. Lenguaje y Kit de Interfaz de Usuario•Kotlin: El
lenguaje de programación principal, aprovechando sus características
de seguridad, concisión y soporte para programación asíncrona.•Jetpack Compose: La interfaz 
usuario es 100% declarativa, construida con Jetpack Compose. Esto permite un desarrollo de UI más
rápido, potente e intuitivo, eliminando la necesidad de archivos XML para los layouts.2. Acceso a 
Datos (Capa de Red)Para la comunicación con el servicio web WorldTimeAPI, se ha implementado una 
capa de red robusta utilizando las siguientes librerías estándar de la industria:•Retrofit: 
Utilizado como cliente HTTP para definir de manera declarativa cómo las respuestas de la API 
se mapean a objetos de datos Kotlin. La interfaz WorldTimeApiService define los endpoints a consumir
.•OkHttp: Funciona como el cliente HTTP subyacente para Retrofit. Se ha configurado con timeouts
específicos para conexión, lectura y escritura, mejorando la resiliencia de la aplicación.
•HttpLoggingInterceptor: Un interceptor de OkHttp configurado para registrar en Logcat todas las
peticiones y respuestas HTTP. Esta herramienta es fundamental para la depuración durante el 
desarrollo.•Gson: Se integra con Retrofit para la serialización y deserialización automática 
de los datos en formato JSON a las data classes de Kotlin (en este caso, WorldTimeResponse).3.
Manejo de Asincronía•Corrutinas de Kotlin (Kotlin Coroutines): Son el pilar para la gestión de 
operaciones asíncronas. La llamada a la red para obtener la hora (fetchMexicoTime) es una 
función de tipo suspend, lo que garantiza que no se bloquee el hilo principal (UI thread), 
previniendo que la aplicación deje de responder. Se utilizan en varios contextos:•LaunchedEffect: 
Para iniciar la carga de datos inicial y para gestionar el ciclo de auto-actualización periódica.
•rememberCoroutineScope: Para obtener un CoroutineScope ligado al ciclo de vida del Composable, 
permitiendo lanzar corrutinas de forma segura desde eventos de la UI, como el clic en un botón.
4.
Gestión de Estado y Lógica de UI•ViewModel Simplificado: La clase TimeViewModel centraliza la 
lógica de negocio y gestiona el estado de la interfaz de usuario. Contiene variables de estado 
(mutableStateOf) para la hora actual, los mensajes de error y el estado de carga (isLoading).
•Estado Reactivo: La interfaz de usuario reacciona automáticamente a los cambios en el TimeViewModel
. El when en HoraMexicoScreen dirige la renderización a uno de los tres estados posibles: 
LoadingState, ErrorState o SuccessState, proporcionando una experiencia de usuario fluida y 
contextual.Funcionalidades Implementadas•Consulta Inicial de Datos: Al iniciar la aplicación,
se realiza una llamada automática a la API para mostrar la hora.•Manejo de Estados de UI:•Estado de 
Carga: Se muestra un indicador de progreso (CircularProgressIndicator) mientras se espera 
la respuesta del servidor.•Estado de Éxito: Se presenta la información formateada, incluyendo 
hora, fecha, día de la semana y datos adicionales como la abreviatura de la zona horaria y el
estado del horario de verano (DST).•Estado de Error: En caso de fallo en la conexión (por ejemplo, 
UnknownHostException o SocketTimeoutException), se muestra un mensaje de error descriptivo y un
botón que permite al usuario reintentar la operación.•Auto-Actualización: Un LaunchedEffect gestiona
un ciclo que vuelve a solicitar los datos cada 30 segundos. Esta funcionalidad puede ser activada o 
desactivada por el usuario a través de un botón en la interfaz.•Actualización Manual: El usuario
tiene a su disposición un botón de "Actualizar" para forzar una nueva llamada a la API en cualquier 
momento.•Formateo de Datos: El TimeViewModel incluye funciones auxiliares para formatear la cadena
de fecha y hora (datetime) recibida de la API a un formato más legible para el usuario
(ej. "HH:mm hrs" y "dd/MM/yyyy").Estructura del Código FuentePara facilitar la comprensión en 
este proyecto, toda la lógica reside en MainActivity.kt. Los componentes clave dentro de este
archivo son:•WorldTimeResponse: Una data class de Kotlin que modela la estructura del JSON devuelto
por la API.•WorldTimeApiService: La interfaz de Retrofit que define la llamada GET.•WorldTimeClient:
Un objeto singleton que proporciona una instancia configurada del cliente Retrofit.•TimeViewModel:
Clase que gestiona el estado y la lógica de negocio.•@Composable HoraMexicoScreen: La función
principal que construye la pantalla, maneja los efectos de ciclo de vida y orquesta los estados 
de la UI.•@Composable LoadingState, ErrorState, SuccessState: Funciones Composable que definen la
apariencia de la UI para cada uno de los posibles estados de la aplicación.
