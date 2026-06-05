from rest_framework import serializers

from learning.models import AcademicTask, BoardScan, Chapter, Module, ReadingProgress, Subject, SubjectPost, Tag


class OwnedRelationMixin:
    owned_relation_fields: dict[str, type] = {}

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        request = self.context.get("request")
        if not request or not request.user.is_authenticated:
            return

        for field_name, model_class in self.owned_relation_fields.items():
            if field_name in self.fields:
                queryset = model_class.objects.filter(owner=request.user)
                field = self.fields[field_name]
                if hasattr(field, "queryset"):
                    field.queryset = queryset
                elif hasattr(field, "child_relation") and hasattr(field.child_relation, "queryset"):
                    field.child_relation.queryset = queryset


class SubjectSerializer(serializers.ModelSerializer):
    class Meta:
        model = Subject
        fields = ["id", "title", "description", "color", "created_at", "updated_at"]
        read_only_fields = ["id", "created_at", "updated_at"]

    def validate_title(self, value: str) -> str:
        request = self.context.get("request")
        if not request or not request.user.is_authenticated:
            return value

        queryset = Subject.objects.filter(owner=request.user, title__iexact=value)
        if self.instance:
            queryset = queryset.exclude(pk=self.instance.pk)
        if queryset.exists():
            raise serializers.ValidationError("You already have a subject with this title.")
        return value


class TagSerializer(serializers.ModelSerializer):
    class Meta:
        model = Tag
        fields = ["id", "name", "color", "created_at"]
        read_only_fields = ["id", "created_at"]

    def validate_name(self, value: str) -> str:
        request = self.context.get("request")
        if not request or not request.user.is_authenticated:
            return value

        queryset = Tag.objects.filter(owner=request.user, name__iexact=value)
        if self.instance:
            queryset = queryset.exclude(pk=self.instance.pk)
        if queryset.exists():
            raise serializers.ValidationError("You already have a tag with this name.")
        return value


class ModuleSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    subject_title = serializers.CharField(source="subject.title", read_only=True)
    module_file_url = serializers.SerializerMethodField()

    owned_relation_fields = {"subject": Subject}

    class Meta:
        model = Module
        fields = [
            "id",
            "subject",
            "subject_title",
            "title",
            "description",
            "content_type",
            "markdown_content",
            "extracted_text",
            "module_file",
            "module_file_url",
            "original_filename",
            "is_favorite",
            "created_at",
            "updated_at",
        ]
        read_only_fields = ["id", "module_file_url", "original_filename", "created_at", "updated_at"]

    def get_module_file_url(self, obj) -> str | None:
        if not obj.module_file:
            return None
        request = self.context.get("request")
        url = obj.module_file.url
        return request.build_absolute_uri(url) if request else url

    def validate_subject(self, subject: Subject) -> Subject:
        request = self.context.get("request")
        if request and subject.owner_id != request.user.id:
            raise serializers.ValidationError("Subject does not belong to the current user.")
        return subject

    def create(self, validated_data):
        module_file = validated_data.get("module_file")
        if module_file:
            validated_data["original_filename"] = module_file.name
        return super().create(validated_data)

    def update(self, instance, validated_data):
        module_file = validated_data.get("module_file")
        if module_file:
            validated_data["original_filename"] = module_file.name
        return super().update(instance, validated_data)


class ChapterSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    module_title = serializers.CharField(source="module.title", read_only=True)

    owned_relation_fields = {"module": Module}

    class Meta:
        model = Chapter
        fields = [
            "id",
            "module",
            "module_title",
            "title",
            "order",
            "markdown_content",
            "extracted_text",
            "created_at",
            "updated_at",
        ]
        read_only_fields = ["id", "module_title", "created_at", "updated_at"]

    def validate_module(self, module: Module) -> Module:
        request = self.context.get("request")
        if request and module.owner_id != request.user.id:
            raise serializers.ValidationError("Module does not belong to the current user.")
        return module


class BoardScanSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    subject_title = serializers.CharField(source="subject.title", read_only=True)
    module_title = serializers.CharField(source="module.title", read_only=True)
    chapter_title = serializers.CharField(source="chapter.title", read_only=True)
    tag_details = TagSerializer(source="tags", many=True, read_only=True)
    image_url = serializers.SerializerMethodField()

    owned_relation_fields = {
        "subject": Subject,
        "module": Module,
        "chapter": Chapter,
        "tags": Tag,
    }

    class Meta:
        model = BoardScan
        fields = [
            "id",
            "subject",
            "subject_title",
            "module",
            "module_title",
            "chapter",
            "chapter_title",
            "image",
            "image_url",
            "raw_ocr_text",
            "cleaned_text",
            "summary",
            "review_status",
            "tags",
            "tag_details",
            "created_at",
            "updated_at",
        ]
        read_only_fields = [
            "id",
            "subject_title",
            "module_title",
            "chapter_title",
            "image_url",
            "tag_details",
            "created_at",
            "updated_at",
        ]

    def get_image_url(self, obj) -> str | None:
        if not obj.image:
            return None
        request = self.context.get("request")
        url = obj.image.url
        return request.build_absolute_uri(url) if request else url

    def validate(self, attrs):
        instance = self.instance
        subject = attrs.get("subject", instance.subject if instance else None)
        module = attrs.get("module", instance.module if instance else None)
        chapter = attrs.get("chapter", instance.chapter if instance else None)

        if chapter:
            if module and chapter.module_id != module.id:
                raise serializers.ValidationError({"chapter": "Chapter must belong to the selected module."})
            if not module:
                attrs["module"] = chapter.module
                module = chapter.module

        if module:
            if subject and module.subject_id != subject.id:
                raise serializers.ValidationError({"module": "Module must belong to the selected subject."})
            if not subject:
                attrs["subject"] = module.subject

        return attrs


class ReadingProgressSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    module_title = serializers.CharField(source="module.title", read_only=True)
    chapter_title = serializers.CharField(source="chapter.title", read_only=True)

    owned_relation_fields = {"module": Module, "chapter": Chapter}

    class Meta:
        model = ReadingProgress
        fields = [
            "id",
            "module",
            "module_title",
            "chapter",
            "chapter_title",
            "progress_percentage",
            "last_position",
            "last_read_at",
            "status",
        ]
        read_only_fields = ["id", "module_title", "chapter_title", "last_read_at"]

    def validate(self, attrs):
        instance = self.instance
        module = attrs.get("module", instance.module if instance else None)
        chapter = attrs.get("chapter", instance.chapter if instance else None)
        progress = attrs.get("progress_percentage")

        if chapter and module and chapter.module_id != module.id:
            raise serializers.ValidationError({"chapter": "Chapter must belong to the selected module."})
        if progress is not None and not 0 <= progress <= 100:
            raise serializers.ValidationError({"progress_percentage": "Progress must be between 0 and 100."})

        return attrs


class AcademicTaskSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    subject_title = serializers.CharField(source="subject.title", read_only=True)
    module_title = serializers.CharField(source="module.title", read_only=True)
    chapter_title = serializers.CharField(source="chapter.title", read_only=True)

    owned_relation_fields = {"subject": Subject, "module": Module, "chapter": Chapter}

    class Meta:
        model = AcademicTask
        fields = [
            "id",
            "subject",
            "subject_title",
            "module",
            "module_title",
            "chapter",
            "chapter_title",
            "title",
            "description",
            "task_type",
            "status",
            "priority",
            "due_at",
            "completed_at",
            "created_at",
            "updated_at",
        ]
        read_only_fields = ["id", "subject_title", "module_title", "chapter_title", "created_at", "updated_at"]

    def validate(self, attrs):
        instance = self.instance
        subject = attrs.get("subject", instance.subject if instance else None)
        module = attrs.get("module", instance.module if instance else None)
        chapter = attrs.get("chapter", instance.chapter if instance else None)

        if module and subject and module.subject_id != subject.id:
            raise serializers.ValidationError({"module": "Module must belong to the selected subject."})
        if chapter:
            if module and chapter.module_id != module.id:
                raise serializers.ValidationError({"chapter": "Chapter must belong to the selected module."})
            if not module:
                attrs["module"] = chapter.module
                module = chapter.module
            if subject and module.subject_id != subject.id:
                raise serializers.ValidationError({"chapter": "Chapter must belong to the selected subject."})

        return attrs


class SubjectPostSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    subject_title = serializers.CharField(source="subject.title", read_only=True)

    owned_relation_fields = {"subject": Subject}

    class Meta:
        model = SubjectPost
        fields = [
            "id",
            "subject",
            "subject_title",
            "title",
            "content",
            "post_type",
            "posted_at",
            "is_pinned",
            "created_at",
            "updated_at",
        ]
        read_only_fields = ["id", "subject_title", "created_at", "updated_at"]

    def validate_subject(self, subject: Subject) -> Subject:
        request = self.context.get("request")
        if request and subject.owner_id != request.user.id:
            raise serializers.ValidationError("Subject does not belong to the current user.")
        return subject


class DashboardStatsSerializer(serializers.Serializer):
    modules_in_progress = serializers.IntegerField()
    notes_saved = serializers.IntegerField()
    quizzes_completed = serializers.IntegerField()
    pending_tasks = serializers.IntegerField()


class DashboardUpcomingItemSerializer(serializers.Serializer):
    type = serializers.CharField()
    id = serializers.IntegerField()
    title = serializers.CharField()
    description = serializers.CharField(allow_blank=True)
    subject = serializers.IntegerField(allow_null=True)
    subject_title = serializers.CharField(allow_blank=True)
    module = serializers.IntegerField(allow_null=True, required=False)
    module_title = serializers.CharField(allow_blank=True, required=False)
    status = serializers.CharField(allow_blank=True, required=False)
    priority = serializers.CharField(allow_blank=True, required=False)
    due_at = serializers.DateTimeField(allow_null=True, required=False)
    posted_at = serializers.DateTimeField(allow_null=True, required=False)


class DashboardContinueLearningSerializer(serializers.Serializer):
    id = serializers.IntegerField()
    module = serializers.IntegerField()
    module_title = serializers.CharField()
    chapter = serializers.IntegerField(allow_null=True)
    chapter_title = serializers.CharField(allow_blank=True)
    progress_percentage = serializers.IntegerField()
    last_position = serializers.CharField(allow_blank=True)
    status = serializers.CharField()
    last_read_at = serializers.DateTimeField()


class DashboardActivityItemSerializer(serializers.Serializer):
    type = serializers.CharField()
    id = serializers.IntegerField()
    title = serializers.CharField()
    description = serializers.CharField(allow_blank=True)
    created_at = serializers.DateTimeField()


class DashboardSerializer(serializers.Serializer):
    overall_progress = serializers.IntegerField()
    stats = DashboardStatsSerializer()
    upcoming = DashboardUpcomingItemSerializer(many=True)
    continue_learning = DashboardContinueLearningSerializer(many=True)
    recent_activity = DashboardActivityItemSerializer(many=True)
