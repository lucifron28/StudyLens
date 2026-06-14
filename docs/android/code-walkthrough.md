# Android Code Walkthrough

This guide shows important snippets from the Android codebase and explains why they are written that way. Snippets are shortened so the main idea is easier to see. Use the linked files in Android Studio for the complete source.

## App Startup

Source files:

- `app/src/main/java/com/example/studylensmobile/MainActivity.kt`
- `app/src/main/java/com/example/studylensmobile/StudyLensApp.kt`

```kotlin
class StudyLensApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
```

`StudyLensApp` is created once when Android starts the app process. It creates `AppContainer`, which holds shared app dependencies such as Retrofit APIs and repositories.

This is used instead of creating repositories inside every screen. The benefit is that the app has one clear dependency source.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as StudyLensApp

        setContent {
            StudyLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, app = app)
                }
            }
        }
    }
}
```

`MainActivity` does not build individual screens directly. It applies the app theme, creates the navigation controller, and hands control to `AppNavGraph`.

Why this is clean:

- Activity code stays short.
- The theme wraps the whole app.
- Navigation is centralized.
- Screens are controlled by routes instead of manual `if` blocks.

## Dependency Container

Source file:

- `app/src/main/java/com/example/studylensmobile/core/network/AppContainer.kt`

```kotlin
class AppContainer(private val context: Context) {
    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val retrofit: Retrofit by lazy {
        RetrofitClient.createRetrofit(tokenManager)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val learningApi: LearningApi by lazy {
        retrofit.create(LearningApi::class.java)
    }

    val aiApi: AiApi by lazy {
        retrofit.create(AiApi::class.java)
    }
}
```

`AppContainer` creates dependencies only when they are first used because of `by lazy`.

The first group creates low-level dependencies:

- `TokenManager` stores auth tokens.
- `Retrofit` sends HTTP requests.
- API interfaces describe backend endpoints.

```kotlin
val authRepository: AuthRepository by lazy {
    AuthRepository(authApi, tokenManager)
}

val subjectsRepository: SubjectsRepository by lazy {
    SubjectsRepository(learningApi)
}

val modulesRepository: ModulesRepository by lazy {
    ModulesRepository(learningApi)
}

val aiRepository: AiRepository by lazy {
    AiRepository(aiApi)
}
```

The second group creates repositories. Screens do not talk to Retrofit directly. They talk to ViewModels, and ViewModels talk to repositories.

Why this is used:

- Repositories are easy to find.
- Dependencies are not duplicated.
- Screens stay focused on UI.
- It is simple enough for the project phase without adding a full dependency injection framework.

## Backend URL

Source file:

- `app/build.gradle.kts`

```kotlin
defaultConfig {
    buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
}
```

The Android emulator cannot call the host computer using `localhost`. Inside the emulator, `localhost` points to the emulator itself.

`10.0.2.2` is the special emulator address for the host computer, so this URL reaches the Django backend running on the development machine.

Why `BuildConfig` is used:

- The URL is configured in Gradle, not scattered through Kotlin files.
- It can be changed later for physical devices or release builds.
- Networking code reads it from one place.

## Retrofit Setup

Source file:

- `app/src/main/java/com/example/studylensmobile/core/network/RetrofitClient.kt`

```kotlin
object RetrofitClient {
    private val baseUrl: String = BuildConfig.API_BASE_URL

    fun createRetrofit(tokenManager: TokenManager): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenRefreshAuthenticator(tokenManager, baseUrl))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

This builds one Retrofit instance for the app.

Important parts:

- `BuildConfig.API_BASE_URL` gives Retrofit the backend URL.
- `HttpLoggingInterceptor` helps debug API calls during development.
- `AuthInterceptor` attaches the JWT access token.
- `TokenRefreshAuthenticator` handles expired access tokens.
- `GsonConverterFactory` converts JSON to Kotlin DTO objects.

Why this is better than manual HTTP code:

- Retrofit interfaces are easier to read.
- JSON conversion is automatic.
- Auth behavior applies to all protected requests.

## Token Storage

Source file:

- `app/src/main/java/com/example/studylensmobile/core/datastore/TokenManager.kt`

```kotlin
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }
}
```

`TokenManager` uses DataStore to save JWT tokens.

The tokens are exposed as `Flow<String?>`, which means the app can observe token changes over time.

```kotlin
suspend fun saveTokens(access: String, refresh: String) {
    context.dataStore.edit { preferences ->
        preferences[ACCESS_TOKEN_KEY] = access
        preferences[REFRESH_TOKEN_KEY] = refresh
    }
}

suspend fun clearTokens() {
    context.dataStore.edit { preferences ->
        preferences.remove(ACCESS_TOKEN_KEY)
        preferences.remove(REFRESH_TOKEN_KEY)
    }
}
```

`saveTokens()` is called after login. `clearTokens()` is called on logout.

Why DataStore is used:

- It is the modern replacement for SharedPreferences.
- It works with coroutines.
- It can be observed with Flow.

## Auth Header

Source file:

- `app/src/main/java/com/example/studylensmobile/core/network/AuthInterceptor.kt`

```kotlin
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (path.contains("/api/auth/register/") ||
            path.contains("/api/auth/token/") ||
            path.contains("/api/auth/token/refresh/")
        ) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenManager.accessToken.firstOrNull() }
        val newRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}
```

The interceptor skips auth endpoints because login and register do not have tokens yet.

For protected endpoints, it adds:

```text
Authorization: Bearer ACCESS_TOKEN
```

Why this is useful:

- Repositories do not manually add headers.
- Every protected request follows the same rule.
- Token logic stays outside feature screens.

## API Interface

Source file:

- `app/src/main/java/com/example/studylensmobile/data/remote/api/LearningApi.kt`

```kotlin
interface LearningApi {
    @GET("api/learning/subjects/")
    suspend fun getSubjects(
        @Query("search") search: String? = null,
        @Query("ordering") ordering: String = "title"
    ): Response<PaginatedSubjectsDto>

    @GET("api/learning/subjects/{subjectId}/overview/")
    suspend fun getSubjectOverview(
        @Path("subjectId") subjectId: String
    ): Response<SubjectOverviewDto>

    @GET("api/learning/modules/{moduleId}/")
    suspend fun getModule(
        @Path("moduleId") moduleId: String
    ): Response<ModuleDto>
}
```

Retrofit reads annotations like `@GET`, `@Path`, and `@Query` and builds HTTP requests.

Example:

```kotlin
getSubjectOverview("3")
```

Becomes:

```text
GET /api/learning/subjects/3/overview/
```

Why this is readable:

- Each Kotlin function maps to one backend endpoint.
- Query parameters are visible in the function signature.
- ViewModels never need to know endpoint strings.

## Response Handling

Source file:

- `app/src/main/java/com/example/studylensmobile/data/remote/ApiResult.kt`

```kotlin
suspend fun <Dto, Domain> apiResult(
    label: String,
    call: suspend () -> Response<Dto>,
    map: (Dto) -> Domain
): Result<Domain> {
    return try {
        call().toResult(label, map)
    } catch (e: Exception) {
        networkFailure(e)
    }
}
```

`apiResult()` runs the Retrofit call, catches network exceptions, and maps a successful DTO into a domain model.

```kotlin
fun <Dto, Domain> Response<Dto>.toResult(
    label: String,
    map: (Dto) -> Domain
): Result<Domain> {
    if (!isSuccessful) {
        return Result.failure(Exception("$label failed (${code()}). Please try again."))
    }

    val body = body()
        ?: return Result.failure(Exception("$label failed: empty server response."))

    return Result.success(map(body))
}
```

This avoids writing the same `try/catch`, `isSuccessful`, and empty-body logic in every repository method.

Why `Result<T>` is used:

- Success and failure are returned in one type.
- ViewModels can call `getOrNull()` and `exceptionOrNull()`.
- Screens only need a readable error message.

## Repository Mapping

Source file:

- `app/src/main/java/com/example/studylensmobile/data/repository/SubjectsRepository.kt`

```kotlin
class SubjectsRepository(
    private val learningApi: LearningApi
) {
    suspend fun getSubjects(search: String? = null): Result<List<Subject>> {
        return apiResult(
            label = "Subjects",
            call = { learningApi.getSubjects(search = search?.takeIf { it.isNotBlank() }) }
        ) { body ->
            body.results.map { it.toDomain() }
        }
    }
}
```

The repository calls `LearningApi`, then converts backend DTOs into domain models.

The `search?.takeIf { it.isNotBlank() }` line prevents sending an empty search query to the backend.

```kotlin
private fun SubjectDto.toDomain(): Subject {
    return Subject(
        id = id.toString(),
        code = title.toSubjectCode(id),
        title = title,
        description = description,
        itemSummary = itemSummary.replace("|", "-"),
        progressPercentage = progressPercentage.coerceIn(0, 100)
    )
}
```

This mapper prepares data for the UI:

- `id` becomes a string because navigation routes use string arguments.
- `code` is generated for compact labels.
- `progressPercentage` is clamped between `0` and `100` so the progress bar receives a valid value.

Why mapping matters:

- DTOs can match backend JSON.
- Domain models can match UI needs.
- Formatting and safety checks stay out of Composables.

## UI State

Source file:

- `app/src/main/java/com/example/studylensmobile/feature/subjects/SubjectsUiState.kt`

```kotlin
data class SubjectsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val subjects: List<Subject> = emptyList(),
    val errorMessage: String? = null
)
```

Each screen has a UI state class that describes everything the screen needs to draw.

For subjects, the screen needs:

- loading state
- refresh state
- current search text
- loaded subject list
- optional error message

Why this is clean:

- Composables do not need many separate variables.
- The whole screen state is easy to inspect.
- Updating state is done by copying the data class.

## ViewModel State Updates

Source file:

- `app/src/main/java/com/example/studylensmobile/feature/subjects/SubjectsViewModel.kt`

```kotlin
class SubjectsViewModel(
    private val subjectsRepository: SubjectsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }
}
```

The private `_uiState` can be changed only inside the ViewModel. The public `uiState` is read-only for screens.

This protects state from being changed directly by Composables.

```kotlin
fun loadSubjects() {
    viewModelScope.launch {
        val hasContent = _uiState.value.subjects.isNotEmpty()
        _uiState.update {
            it.copy(
                isLoading = !hasContent,
                isRefreshing = hasContent,
                errorMessage = null
            )
        }

        val result = subjectsRepository.getSubjects(_uiState.value.searchQuery)

        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                subjects = result.getOrNull() ?: it.subjects,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }
}
```

This function shows the standard loading pattern:

1. Set loading or refreshing state.
2. Call the repository.
3. Keep old content if the request fails.
4. Store the error message if there is one.

Why old content is kept:

- If refresh fails, the user still sees the previous subjects.
- The app feels more stable during temporary network issues.

## Compose Screen

Source file:

- `app/src/main/java/com/example/studylensmobile/feature/subjects/SubjectsScreen.kt`

```kotlin
@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    onNavigateToSubjectDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "Subjects",
                actions = {
                    IconButton(onClick = viewModel::loadSubjects) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh subjects"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Loading subjects...",
                    modifier = Modifier.padding(padding).fillMaxSize()
                )
            }
            uiState.subjects.isEmpty() && uiState.errorMessage != null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Subjects are unavailable.",
                    onRetry = viewModel::loadSubjects,
                    modifier = Modifier.padding(padding).fillMaxSize()
                )
            }
            else -> {
                SubjectsContent(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::loadSubjects,
                    onRetry = viewModel::loadSubjects,
                    onNavigateToSubjectDetail = onNavigateToSubjectDetail,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
```

The screen observes `uiState` with `collectAsState()`. When the ViewModel updates the state, Compose redraws the screen.

The screen chooses between:

- loading UI
- full-page error UI
- normal content UI

Why this is a good Compose pattern:

- UI is a function of state.
- Loading and error branches are explicit.
- The screen delegates reusable UI to shared components.

## Navigation Routes

Source file:

- `app/src/main/java/com/example/studylensmobile/ui/navigation/AppRoutes.kt`

```kotlin
object AppRoutes {
    const val SUBJECT_DETAIL = "subjectDetail/{subjectId}"
    const val MODULE_READER = "moduleReader/{moduleId}"
    const val AI_SUMMARY = "aiSummary/{sourceType}/{sourceId}"

    fun createSubjectDetailRoute(subjectId: String) = "subjectDetail/$subjectId"
    fun createModuleReaderRoute(moduleId: String) = "moduleReader/$moduleId"
    fun createAiSummaryRoute(sourceType: String, sourceId: String) =
        "aiSummary/$sourceType/$sourceId"
}
```

Constants define route patterns. Builder functions create real routes with actual IDs.

Why builder functions are used:

- Route strings are not typed manually in every screen.
- Typos are less likely.
- Route construction is easy to change later.

## Navigation Graph

Source file:

- `app/src/main/java/com/example/studylensmobile/ui/navigation/AppNavGraph.kt`

```kotlin
composable(AppRoutes.SUBJECTS) {
    val subjectsViewModel: SubjectsViewModel = viewModel(
        factory = viewModelFactory {
            SubjectsViewModel(app.container.subjectsRepository)
        }
    )

    SubjectsScreen(
        viewModel = subjectsViewModel,
        onNavigateToSubjectDetail = { subjectId ->
            navController.navigate(AppRoutes.createSubjectDetailRoute(subjectId))
        }
    )
}
```

This route creates the `SubjectsViewModel`, passes it to the screen, and provides navigation callbacks.

The screen does not receive `NavController` directly. It receives `onNavigateToSubjectDetail`.

Why callbacks are used:

- Screens are easier to test and preview.
- Navigation logic stays inside `AppNavGraph`.
- Feature screens do not need to know route strings.

## ViewModel Factory

Source file:

- `app/src/main/java/com/example/studylensmobile/core/viewmodel/ViewModelFactory.kt`

```kotlin
inline fun <reified VM : ViewModel> viewModelFactory(
    crossinline create: () -> VM
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VM::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return create() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
```

This helper lets `AppNavGraph` create ViewModels that need constructor parameters.

Example:

```kotlin
viewModel(
    factory = viewModelFactory {
        ModuleReaderViewModel(
            moduleId = moduleId,
            modulesRepository = app.container.modulesRepository
        )
    }
)
```

Why this helper exists:

- Android's default `viewModel()` only works easily with empty constructors.
- Most StudyLens ViewModels need repositories or route IDs.
- This avoids creating one factory class for every ViewModel.

## Shared Screen States

Source file:

- `app/src/main/java/com/example/studylensmobile/ui/components/ScreenState.kt`

```kotlin
@Composable
fun StudyLensErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "Retry"
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(retryLabel)
        }
    }
}
```

This shared error component is reused across screens.

Why this is better than repeating error UI:

- Error styling stays consistent.
- Feature screens stay shorter.
- Retry behavior is always clear.

## AI Study Tool Flow

Source files:

- `app/src/main/java/com/example/studylensmobile/data/remote/api/AiApi.kt`
- `app/src/main/java/com/example/studylensmobile/data/repository/AiRepository.kt`
- `app/src/main/java/com/example/studylensmobile/feature/studytools/AiSummaryViewModel.kt`

```kotlin
interface AiApi {
    @POST("api/ai/summarize/")
    suspend fun summarize(@Body request: SummaryRequest): Response<SummaryDto>

    @POST("api/ai/generate-flashcards/")
    suspend fun generateFlashcards(
        @Body request: GenerateFlashcardsRequest
    ): Response<List<FlashcardDto>>
}
```

Android calls the backend AI endpoints. It does not call Ollama or Gemini directly.

```kotlin
suspend fun generateSummary(sourceType: String, sourceId: String): Result<Summary> {
    val source = sourceFor(sourceType, sourceId)
        ?: return Result.failure(Exception("Unsupported or invalid summary source."))

    val request = SummaryRequest(
        moduleId = source.moduleId,
        chapterId = source.chapterId,
        boardScanId = source.boardScanId
    )

    return apiResult("Summary", { aiApi.summarize(request) }) {
        it.toDomain()
    }
}
```

`AiRepository` converts a route source like `module/5` or `board_scan/8` into the request body expected by the backend.

Why the AI call goes through the backend:

- Android does not store AI provider keys.
- Prompt templates stay server-side.
- Backend can validate AI JSON before saving generated study tools.
- The app only needs stable endpoints.

```kotlin
class AiSummaryViewModel(
    private val sourceType: String,
    private val sourceId: String,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiSummaryUiState())
    val uiState: StateFlow<AiSummaryUiState> = _uiState.asStateFlow()

    init {
        generateSummary()
    }
}
```

The ViewModel receives route arguments and immediately generates a summary when the screen opens.

This keeps the screen simple. The screen only displays loading, error, and summary states.

## How to Read This Codebase

When tracing a feature, follow this order:

1. Start at the route in `AppNavGraph.kt`.
2. Open the screen file in `feature/...`.
3. Check the `UiState` class to see what the screen displays.
4. Open the ViewModel to see what actions exist.
5. Open the repository to see what backend data is loaded.
6. Open the Retrofit API interface to see the endpoint.
7. Open the DTO and domain model to see how data is shaped.

Example for subjects:

```text
AppNavGraph.kt
-> SubjectsScreen.kt
-> SubjectsUiState.kt
-> SubjectsViewModel.kt
-> SubjectsRepository.kt
-> LearningApi.kt
-> SubjectDtos.kt
-> Subject.kt
```

This is the same pattern used by modules, board scans, and study tools.
