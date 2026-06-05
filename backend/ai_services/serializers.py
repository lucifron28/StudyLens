from rest_framework import serializers

from ai_services.models import TutorMessage, TutorSession
from learning.models import BoardScan, Chapter, Module
from studytools.serializers import OwnedRelationMixin, SourceValidationMixin


class SourceRequestSerializer(serializers.Serializer):
    module_id = serializers.IntegerField(required=False)
    chapter_id = serializers.IntegerField(required=False)
    board_scan_id = serializers.IntegerField(required=False)
    text = serializers.CharField(required=False, allow_blank=True)

    def validate(self, attrs):
        if not any(attrs.get(field) for field in ["module_id", "chapter_id", "board_scan_id", "text"]):
            raise serializers.ValidationError("Provide module_id, chapter_id, board_scan_id, or text.")
        return attrs


class SummarizeRequestSerializer(SourceRequestSerializer):
    pass


class GenerateFlashcardsRequestSerializer(SourceRequestSerializer):
    count = serializers.IntegerField(required=False, min_value=1, max_value=30, default=5)


class GenerateQuizRequestSerializer(SourceRequestSerializer):
    count = serializers.IntegerField(required=False, min_value=1, max_value=30, default=5)
    question_type = serializers.ChoiceField(
        required=False,
        choices=["multiple_choice", "true_false", "short_answer"],
    )


class StartTutorRequestSerializer(serializers.Serializer):
    module_id = serializers.IntegerField(required=False)
    chapter_id = serializers.IntegerField(required=False)
    board_scan_id = serializers.IntegerField(required=False)
    title = serializers.CharField(required=False, allow_blank=True, max_length=200)

    def validate(self, attrs):
        if not any(attrs.get(field) for field in ["module_id", "chapter_id", "board_scan_id"]):
            raise serializers.ValidationError("Provide module_id, chapter_id, or board_scan_id.")
        if not attrs.get("title"):
            attrs.pop("title", None)
        return attrs


class TutorMessageRequestSerializer(serializers.Serializer):
    session_id = serializers.IntegerField()
    message = serializers.CharField(allow_blank=False)


class TutorSessionSerializer(SourceValidationMixin, OwnedRelationMixin, serializers.ModelSerializer):
    module_title = serializers.CharField(source="module.title", read_only=True)
    chapter_title = serializers.CharField(source="chapter.title", read_only=True)

    owned_relation_fields = {"module": Module, "chapter": Chapter, "board_scan": BoardScan}

    class Meta:
        model = TutorSession
        fields = [
            "id",
            "module",
            "module_title",
            "chapter",
            "chapter_title",
            "board_scan",
            "title",
            "status",
            "clear_answers_count",
            "target_clear_answers",
            "created_at",
            "updated_at",
        ]
        read_only_fields = [
            "id",
            "module_title",
            "chapter_title",
            "clear_answers_count",
            "created_at",
            "updated_at",
        ]


class TutorMessageSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    session_title = serializers.CharField(source="session.title", read_only=True)

    owned_relation_fields = {"session": TutorSession}

    class Meta:
        model = TutorMessage
        fields = [
            "id",
            "session",
            "session_title",
            "role",
            "content",
            "clarity_result",
            "created_at",
        ]
        read_only_fields = ["id", "session_title", "created_at"]

    def validate_session(self, session: TutorSession) -> TutorSession:
        request = self.context.get("request")
        if request and session.owner_id != request.user.id:
            raise serializers.ValidationError("Tutor session does not belong to the current user.")
        return session


class TutorStartResponseSerializer(serializers.Serializer):
    session = TutorSessionSerializer()
    message = TutorMessageSerializer()


class TutorMessageResponseSerializer(serializers.Serializer):
    session = TutorSessionSerializer()
    message = TutorMessageSerializer()

