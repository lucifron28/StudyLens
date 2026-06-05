from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView

from accounts.views import EmailOrUsernameTokenObtainPairView, MeView, RegisterView


urlpatterns = [
    path("register/", RegisterView.as_view(), name="auth-register"),
    path("token/", EmailOrUsernameTokenObtainPairView.as_view(), name="token-obtain-pair"),
    path("token/refresh/", TokenRefreshView.as_view(), name="token-refresh"),
    path("me/", MeView.as_view(), name="auth-me"),
]
