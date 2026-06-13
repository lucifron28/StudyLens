# Networking and Authentication

The Android app communicates with the Django backend using Retrofit and JWT authentication.

## Main Files

```text
core/network/RetrofitClient.kt
core/network/AuthInterceptor.kt
core/network/TokenRefreshAuthenticator.kt
core/network/AppContainer.kt
core/datastore/TokenManager.kt
data/remote/api/AuthApi.kt
data/repository/AuthRepository.kt
```

## RetrofitClient

`RetrofitClient.kt` creates the shared Retrofit instance.

It configures:

- backend base URL from `BuildConfig.API_BASE_URL`
- Gson JSON converter
- OkHttp client
- auth interceptor
- token refresh authenticator
- debug logging interceptor

Why:

- All HTTP configuration lives in one place.
- Changing the base URL or auth behavior does not require editing every API call.

## AuthInterceptor

Adds this header when an access token exists:

```text
Authorization: Bearer ACCESS_TOKEN
```

Why:

- Screens and repositories do not need to manually attach tokens.
- Every protected API call uses the same auth behavior.

## TokenRefreshAuthenticator

When the backend rejects an expired access token, this authenticator tries to refresh it using the refresh token.

Why:

- The user does not need to log in again every time the access token expires.
- Refresh logic stays in networking code instead of feature screens.

## TokenManager

Stores:

- access token
- refresh token

Why DataStore is used:

- asynchronous and safe for modern Android
- exposes tokens as Flow
- works cleanly with Compose state and app startup logic

## AuthApi

Retrofit interface for backend auth endpoints:

```text
POST api/auth/register/
POST api/auth/token/
POST api/auth/token/refresh/
GET  api/auth/me/
```

## AuthRepository

Wraps `AuthApi` and `TokenManager`.

Responsibilities:

- register user
- log in user
- save tokens
- load current user
- clear tokens on logout
- return `Result<T>` values for ViewModels

## Startup Auth Check

`AppNavGraph` observes the saved access token:

```text
if token is empty -> login
else -> home
```

This keeps the startup rule simple.

## Logout

Logout clears saved tokens and navigates back to `login`.

The backend does not need to be called for logout in this first version because JWT logout can be handled locally by removing the tokens.
