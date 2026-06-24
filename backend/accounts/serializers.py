from django.contrib.auth import get_user_model
from django.utils.text import slugify
from rest_framework import serializers
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer


User = get_user_model()


class UserSerializer(serializers.ModelSerializer):
    profileImageUrl = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ["id", "username", "email", "first_name", "last_name", "profileImageUrl"]
        read_only_fields = ["id", "username", "email", "first_name", "last_name"]

    def get_profileImageUrl(self, obj):
        if hasattr(obj, "profile") and obj.profile.profile_image:
            request = self.context.get("request")
            if request:
                return request.build_absolute_uri(obj.profile.profile_image.url)
            return obj.profile.profile_image.url
        return None


class RegisterSerializer(serializers.ModelSerializer):
    username = serializers.CharField(required=False, allow_blank=True)
    email = serializers.EmailField(required=True)
    password = serializers.CharField(write_only=True, min_length=8)
    first_name = serializers.CharField(required=False, allow_blank=True)
    last_name = serializers.CharField(required=False, allow_blank=True)

    class Meta:
        model = User
        fields = ["id", "username", "email", "password", "first_name", "last_name"]
        read_only_fields = ["id"]

    def validate_email(self, value: str) -> str:
        if value and User.objects.filter(email__iexact=value).exists():
            raise serializers.ValidationError("A user with this email already exists.")
        return value

    def validate_username(self, value: str) -> str:
        if value and User.objects.filter(username__iexact=value).exists():
            raise serializers.ValidationError("A user with this username already exists.")
        return value

    def _generate_username(self, email: str) -> str:
        base = slugify(email.split("@", 1)[0]).replace("-", "_") or "student"
        base = base[:140]
        candidate = base
        counter = 1

        while User.objects.filter(username__iexact=candidate).exists():
            suffix = f"_{counter}"
            candidate = f"{base[:150 - len(suffix)]}{suffix}"
            counter += 1

        return candidate

    def create(self, validated_data):
        password = validated_data.pop("password")
        if not validated_data.get("username"):
            validated_data["username"] = self._generate_username(validated_data["email"])
        user = User(**validated_data)
        user.set_password(password)
        user.save()
        return user


class EmailOrUsernameTokenObtainPairSerializer(TokenObtainPairSerializer):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.fields[self.username_field].required = False
        self.fields[self.username_field].allow_blank = True
        self.fields["email"] = serializers.EmailField(required=False, allow_blank=True)

    def validate(self, attrs):
        username = attrs.get("username", "").strip()
        email = attrs.get("email", "").strip()

        if not username and not email:
            raise serializers.ValidationError("Provide either username or email.")

        if email and not username:
            user = User.objects.filter(email__iexact=email).first()
            if user:
                attrs["username"] = user.get_username()
            else:
                # Keep the error generic so login does not reveal registered emails.
                attrs["username"] = email

        return super().validate(attrs)
