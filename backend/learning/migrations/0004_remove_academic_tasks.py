from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ("learning", "0003_readingprogress_unique_reading_progress_per_target"),
    ]

    operations = [
        migrations.DeleteModel(
            name="AcademicTask",
        ),
        migrations.AlterField(
            model_name="subjectpost",
            name="post_type",
            field=models.CharField(
                choices=[
                    ("announcement", "Announcement"),
                    ("reminder", "Reminder"),
                    ("update", "Update"),
                ],
                default="announcement",
                max_length=20,
            ),
        ),
    ]
