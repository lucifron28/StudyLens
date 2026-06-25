from rest_framework import serializers

from learning.models import BoardScan, Chapter, Module, ReadingProgress, StudyTask, Subject, Tag
from learning.services.extraction import ExtractionError, extract_pdf_text, extract_docx_text, extract_pptx_text

MAX_MODULE_FILE_SIZE_BYTES = 20 * 1024 * 1024
MODULE_FILE_EXTENSIONS = {
    Module.ContentType.PDF: ".pdf",
    Module.ContentType.DOCX: ".docx",
    Module.ContentType.PPTX: ".pptx",
}


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
    module_count = serializers.SerializerMethodField()
    board_scan_count = serializers.SerializerMethodField()
    task_count = serializers.SerializerMethodField()
    progress_percentage = serializers.SerializerMethodField()
    item_summary = serializers.SerializerMethodField()

    class Meta:
        model = Subject
        fields = [
            "id",
            "title",
            "description",
            "color",
            "module_count",
            "board_scan_count",
            "task_count",
            "progress_percentage",
            "item_summary",
            "created_at",
            "updated_at",
        ]
        read_only_fields = [
            "id",
            "module_count",
            "board_scan_count",
            "task_count",
            "progress_percentage",
            "item_summary",
            "created_at",
            "updated_at",
        ]

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

    def get_module_count(self, obj) -> int:
        return getattr(obj, "module_count_value", None) or obj.modules.count()

    def get_board_scan_count(self, obj) -> int:
        return getattr(obj, "board_scan_count_value", None) or obj.board_scans.count()

    def get_task_count(self, obj) -> int:
        return getattr(obj, "task_count_value", None) or obj.tasks.count()

    def get_progress_percentage(self, obj) -> int:
        return round(getattr(obj, "progress_average", None) or 0)

    def get_item_summary(self, obj) -> str:
        return f"{self.get_module_count(obj)} Modules | {self.get_board_scan_count(obj)} Notes | {self.get_task_count(obj)} Tasks"


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

    def validate(self, attrs):
        module_file = attrs.get("module_file")
        if not module_file:
            return attrs

        content_type = attrs.get(
            "content_type",
            self.instance.content_type if self.instance else Module.ContentType.MARKDOWN,
        )
        expected_extension = MODULE_FILE_EXTENSIONS.get(content_type)
        if not expected_extension:
            raise serializers.ValidationError(
                {"module_file": "Files can only be uploaded as PDF, DOCX, or PPTX modules."}
            )
        if module_file.size > MAX_MODULE_FILE_SIZE_BYTES:
            raise serializers.ValidationError({"module_file": "Module files must be 20 MB or smaller."})
        if not module_file.name.lower().endswith(expected_extension):
            raise serializers.ValidationError(
                {"module_file": f"Selected file must use the {expected_extension} extension."}
            )

        return attrs

    def create(self, validated_data):
        module_file = validated_data.get("module_file")
        if module_file:
            validated_data["original_filename"] = module_file.name
            content_type = validated_data.get("content_type", Module.ContentType.MARKDOWN)
            if content_type == Module.ContentType.PDF:
                try:
                    validated_data["extracted_text"] = extract_pdf_text(module_file)
                except ExtractionError:
                    pass
            elif content_type == Module.ContentType.DOCX:
                try:
                    validated_data["extracted_text"] = extract_docx_text(module_file)
                except ExtractionError:
                    pass
            elif content_type == Module.ContentType.PPTX:
                try:
                    validated_data["extracted_text"] = extract_pptx_text(module_file)
                except ExtractionError:
                    pass
        return super().create(validated_data)

    def update(self, instance, validated_data):
        module_file = validated_data.get("module_file")
        if module_file:
            validated_data["original_filename"] = module_file.name
            content_type = validated_data.get("content_type", instance.content_type)
            if content_type == Module.ContentType.PDF:
                try:
                    validated_data["extracted_text"] = extract_pdf_text(module_file)
                except ExtractionError:
                    pass
            elif content_type == Module.ContentType.DOCX:
                try:
                    validated_data["extracted_text"] = extract_docx_text(module_file)
                except ExtractionError:
                    pass
            elif content_type == Module.ContentType.PPTX:
                try:
                    validated_data["extracted_text"] = extract_pptx_text(module_file)
                except ExtractionError:
                    pass
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


class StudyTaskSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    subject_title = serializers.CharField(source="subject.title", read_only=True)

    owned_relation_fields = {"subject": Subject}

    class Meta:
        model = StudyTask
        fields = [
            "id",
            "subject",
            "subject_title",
            "title",
            "content",
            "task_type",
            "is_completed",
            "due_date",
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


class DashboardUpcomingItemSerializer(serializers.Serializer):
    type = serializers.CharField()
    id = serializers.IntegerField()
    title = serializers.CharField()
    description = serializers.CharField(allow_blank=True)
    subject = serializers.IntegerField(allow_null=True)
    subject_title = serializers.CharField(allow_blank=True)
    module = serializers.IntegerField(allow_null=True, required=False)
    module_title = serializers.CharField(allow_blank=True, required=False)
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


class SubjectOverviewSerializer(serializers.Serializer):
    id = serializers.IntegerField()
    title = serializers.CharField()
    description = serializers.CharField(allow_blank=True)
    module_count = serializers.IntegerField()
    board_scan_count = serializers.IntegerField()
    task_count = serializers.IntegerField()
    progress_percentage = serializers.IntegerField()
    latest_modules = serializers.ListField(child=serializers.DictField())
    recent_board_scans = serializers.ListField(child=serializers.DictField())
    tasks = serializers.ListField(child=serializers.DictField())
