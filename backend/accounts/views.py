from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework_simplejwt.views import TokenObtainPairView
from rest_framework.parsers import MultiPartParser, FormParser
from django.core.files.storage import default_storage

from accounts.serializers import EmailOrUsernameTokenObtainPairSerializer, RegisterSerializer, UserSerializer


class RegisterView(generics.CreateAPIView):
    serializer_class = RegisterSerializer
    permission_classes = [permissions.AllowAny]


class MeView(generics.RetrieveUpdateAPIView):
    serializer_class = UserSerializer

    def get_object(self):
        return self.request.user


class ProfileImageUploadView(generics.UpdateAPIView):
    serializer_class = UserSerializer
    parser_classes = [MultiPartParser, FormParser]

    def update(self, request, *args, **kwargs):
        user = request.user
        file_obj = request.FILES.get('image')
        if not file_obj:
            return Response({"error": "No image provided"}, status=status.HTTP_400_BAD_REQUEST)

        # The post_save signal should have created a profile, but just in case:
        if not hasattr(user, 'profile'):
            from accounts.models import UserProfile
            UserProfile.objects.create(user=user)

        user.profile.profile_image = file_obj
        user.profile.save()

        serializer = self.get_serializer(user)
        return Response(serializer.data)


class EmailOrUsernameTokenObtainPairView(TokenObtainPairView):
    serializer_class = EmailOrUsernameTokenObtainPairSerializer
