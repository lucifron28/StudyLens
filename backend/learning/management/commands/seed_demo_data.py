from __future__ import annotations

from datetime import timedelta

from django.contrib.auth import get_user_model
from django.core.management.base import BaseCommand
from django.utils import timezone

from ai_services.models import TutorMessage, TutorSession
from learning.models import BoardScan, Chapter, Module, ReadingProgress, StudyTask, Subject, Tag
from studytools.models import Difficulty, Flashcard, Quiz, QuizAttempt, QuizQuestion, SourceType, Summary


class Command(BaseCommand):
    help = "Create StudyLens prototype-like sample data for local testing."

    def add_arguments(self, parser):
        parser.add_argument("--username", default="ron")
        parser.add_argument("--password", default="Demo1234!")
        parser.add_argument("--email", default="ron@example.com")
        parser.add_argument("--first-name", default="Ron Vincent")
        parser.add_argument("--last-name", default="Cada")

    def handle(self, *args, **options):
        now = timezone.now()
        user = self._upsert_user(options)

        self._clear_user_demo_data(user)

        subjects = self._create_subjects(user)
        tags = self._create_tags(user)
        modules, chapters = self._create_modules_and_chapters(user, subjects)
        self._create_study_tasks(user, subjects, now)
        scans = self._create_board_scans(user, subjects, modules, chapters, tags, now)
        self._create_reading_progress(user, modules, chapters)
        self._create_study_tools(user, modules, chapters, scans, now)
        self._create_tutor_session(user, modules, chapters)

        self.stdout.write(
            self.style.SUCCESS(
                "StudyLens demo data ready for "
                f"{options['email']} / {options['password']}."
            )
        )

    def _upsert_user(self, options):
        User = get_user_model()
        user, _ = User.objects.get_or_create(username=options["username"])
        user.email = options["email"]
        user.first_name = options["first_name"]
        user.last_name = options["last_name"]
        user.set_password(options["password"])
        user.save()
        return user

    def _clear_user_demo_data(self, user):
        QuizAttempt.objects.filter(owner=user).delete()
        Quiz.objects.filter(owner=user).delete()
        Flashcard.objects.filter(owner=user).delete()
        Summary.objects.filter(owner=user).delete()
        TutorSession.objects.filter(owner=user).delete()
        ReadingProgress.objects.filter(owner=user).delete()
        BoardScan.objects.filter(owner=user).delete()
        StudyTask.objects.filter(owner=user).delete()
        Chapter.objects.filter(owner=user).delete()
        Module.objects.filter(owner=user).delete()
        Tag.objects.filter(owner=user).delete()
        Subject.objects.filter(owner=user).delete()

    def _create_subjects(self, user):
        data = [
            (
                "Native Android Development",
                "Building modern Android apps with Kotlin, Jetpack Compose, and clean app architecture.",
                "#000A67",
            ),
            (
                "Database Systems",
                "Relational databases, SQL fundamentals, ER modeling, normalization, and transactions.",
                "#007A78",
            ),
            (
                "Software Engineering",
                "SDLC, requirements, prototyping, design patterns, testing, and project delivery.",
                "#252B5C",
            ),
            (
                "Calculus Concepts",
                "Limits, derivatives, and integrals for technical problem solving.",
                "#5651A6",
            ),
            (
                "C Programming Fundamentals",
                "Introduction to C programming, memory management, pointers, and data structures.",
                "#D44000",
            ),
        ]
        subjects = {}
        for title, description, color in data:
            subject, _ = Subject.objects.update_or_create(
                owner=user,
                title=title,
                defaults={"description": description, "color": color},
            )
            subjects[title] = subject
        return subjects

    def _create_tags(self, user):
        data = [
            ("architecture", "#E0E7FF"),
            ("android", "#CCFBF1"),
            ("review", "#FFE4E6"),
            ("database", "#DBEAFE"),
            ("important", "#FEF3C7"),
        ]
        tags = {}
        for name, color in data:
            tag, _ = Tag.objects.update_or_create(
                owner=user,
                name=name,
                defaults={"color": color},
            )
            tags[name] = tag
        return tags

    def _create_modules_and_chapters(self, user, subjects):
        module_specs = {
            "Native Android Development": [
                ("Intro to Android UI", "Layouts, Material components, and navigation patterns.", True),
                ("UI Layouts", "ConstraintLayout basics, Compose layout primitives, and responsive UI.", True),
                ("Kotlin Basics", "Variables, functions, classes, null safety, and collections.", False),
                ("Jetpack Compose Basics", "Composable functions, state, recomposition, and Material 3.", True),
                ("ViewModel and State", "Lifecycle-aware state holders and UI state streams.", False),
                ("Navigation Compose", "Screen routes, back stack behavior, and bottom navigation.", False),
                ("CameraX Board Scanning", "Camera capture flow for classroom whiteboard notes.", False),
                ("Offline Data Storage", "Local cache ideas for future Room/DataStore support.", False),
            ],
            "Database Systems": [
                ("SQL Fundamentals", "SELECT, JOIN, GROUP BY, filtering, and aggregate queries.", True),
                ("ER Diagrams", "Entities, attributes, relationships, cardinality, and notation.", True),
                ("Normalization", "1NF, 2NF, 3NF, and reducing update anomalies.", False),
                ("Transactions", "ACID properties, commits, rollbacks, and isolation levels.", False),
                ("Indexing Basics", "How indexes speed up common lookup queries.", False),
                ("PostgreSQL in Docker", "Running PostgreSQL locally with Docker Compose.", False),
            ],
            "Software Engineering": [
                ("Final Prototype", "Preparing a presentable midterm prototype and demo flow.", True),
                ("SDLC Overview", "Planning, analysis, design, implementation, testing, and maintenance.", False),
                ("Use Case Diagrams", "Actors, system boundaries, and user goals.", False),
                ("Design Patterns", "Reusable solutions for common software design problems.", False),
                ("Testing Basics", "Unit tests, integration tests, and acceptance checks.", False),
            ],
            "Calculus Concepts": [
                ("Limits Refresher", "Understanding behavior near a point."),
                ("Derivatives", "Rates of change and tangent lines."),
                ("Integrals", "Area under curves and accumulation."),
            ],
            "C Programming Fundamentals": [
                ("Pointers and Memory", "Understanding how pointers work and managing memory manually.", True),
                ("Data Structures", "Implementing linked lists, stacks, and queues in C.", False),
            ],
        }

        chapter_specs = {
            "Intro to Android UI": [
                ("UI Patterns", "Android UI should be consistent, accessible, and easy to scan."),
                ("Material Components", "Cards, chips, top bars, and navigation bars provide familiar structure."),
            ],
            "UI Layouts": [
                (
                    "ConstraintLayout Basics",
                    "ConstraintLayout allows developers to create responsive layouts using relationships between views. "
                    "It helps reduce nested layouts and improves UI performance.",
                ),
                (
                    "Compose Layouts",
                    "Column, Row, Box, LazyColumn, and Scaffold are the main building blocks of Compose screens.",
                ),
            ],
            "SQL Fundamentals": [
                ("Basic Queries", "SELECT reads rows, WHERE filters them, and ORDER BY sorts the result."),
                ("Joins", "JOIN combines related data from multiple tables using matching keys."),
            ],
            "ER Diagrams": [
                ("Entities and Relationships", "ER diagrams describe tables, attributes, and how data connects."),
            ],
            "Final Prototype": [
                ("Prototype Checklist", "Screens should be consistent, linked, and ready for presentation."),
            ],
        }

        modules = {}
        chapters = {}
        for subject_title, specs in module_specs.items():
            subject = subjects[subject_title]
            for order, spec in enumerate(specs, start=1):
                title, description = spec[0], spec[1]
                is_favorite = bool(spec[2]) if len(spec) > 2 else False
                module, _ = Module.objects.update_or_create(
                    owner=user,
                    subject=subject,
                    title=title,
                    defaults={
                        "description": description,
                        "content_type": Module.ContentType.MARKDOWN,
                        "markdown_content": f"# {title}\n\n{description}",
                        "extracted_text": description,
                        "is_favorite": is_favorite,
                    },
                )
                modules[title] = module

                default_chapters = chapter_specs.get(title) or [
                    ("Overview", description),
                    ("Practice Notes", f"Review the key ideas from {title} and answer short practice questions."),
                ]
                for chapter_order, (chapter_title, content) in enumerate(default_chapters, start=1):
                    chapter, _ = Chapter.objects.update_or_create(
                        owner=user,
                        module=module,
                        order=chapter_order,
                        defaults={
                            "title": chapter_title,
                            "markdown_content": content,
                            "extracted_text": content,
                        },
                    )
                    chapters[(title, chapter_title)] = chapter

        return modules, chapters

    def _create_study_tasks(self, user, subjects, now):
        cs_subject = list(subjects.values())[0]
        tasks_to_create = [
            (
                "Read Chapter 1",
                "Don't forget to read chapter 1 of Introduction before next week's lecture.",
                StudyTask.TaskType.TODO,
                True,
            ),
            (
                "CS50 Lecture Notes Uploaded",
                "Uploaded the notes from the CS50 introductory lecture.",
                StudyTask.TaskType.NOTE,
                False,
            ),
            (
                "Midterm Date Changed",
                "The midterm has been pushed back to November 15th.",
                StudyTask.TaskType.REMINDER,
                False,
            ),
        ]

        c_tasks_to_create = [
            (
                "Finish Linked List Assignment",
                "Due next Friday. Must use malloc and free correctly.",
                StudyTask.TaskType.TODO,
                True,
            ),
            (
                "Midterm 1 Topics",
                "Pointers, arrays, and basic dynamic memory.",
                StudyTask.TaskType.NOTE,
                False,
            ),
            (
                "Debug Session with TA",
                "Went over memory leaks in valgrind. Need to remember to free all mallocs in reverse order.",
                StudyTask.TaskType.LOG,
                False,
            ),
        ]

        c_subject = subjects.get("C Programming Fundamentals")
        if c_subject:
            for title, content, task_type, is_pinned in c_tasks_to_create:
                StudyTask.objects.update_or_create(
                    owner=user,
                    subject=c_subject,
                    title=title,
                    defaults={
                        "content": content,
                        "task_type": task_type,
                        "is_pinned": is_pinned,
                    },
                )

        for title, content, task_type, is_pinned in tasks_to_create:
            StudyTask.objects.update_or_create(
                owner=user,
                subject=cs_subject,
                title=title,
                defaults={
                    "content": content,
                    "task_type": task_type,
                    "is_pinned": is_pinned,
                },
            )

    def _create_board_scans(self, user, subjects, modules, chapters, tags, now):
        scan_data = [
            (
                "Native Android Development",
                "ViewModel and State",
                "Overview",
                "Sept 14 - System Architecture",
                "ViewModel connects UI state with repository. LiveData observes lifecycle-aware data changes. Repository handles data sources.",
                "Architecture note for StudyLens module reader, repository, and backend API flow.",
                BoardScan.ReviewStatus.NEEDS_REVIEW,
                ["architecture", "android", "review"],
                now - timedelta(hours=1),
            ),
            (
                "Database Systems",
                "ER Diagrams",
                "Entities and Relationships",
                "Sept 12 - ER Diagrams",
                "Entities map to tables. Relationships define how records connect. Cardinality shows one-to-one, one-to-many, or many-to-many.",
                "ER diagram review for database relationships and cardinality.",
                BoardScan.ReviewStatus.REVIEWED,
                ["database", "review"],
                now - timedelta(days=1, hours=2),
            ),
            (
                "Calculus Concepts",
                "Derivatives",
                "Overview",
                "Sept 05 - Calculus Concepts",
                "Derivative represents rate of change. The slope of a tangent line can estimate motion or growth at an instant.",
                "Calculus board note about derivatives and tangent slope.",
                BoardScan.ReviewStatus.NEW,
                ["important"],
                now - timedelta(days=3),
            ),
            (
                "C Programming Fundamentals",
                "Pointers and Memory",
                "Overview",
                "Oct 01 - Pointer Arithmetic",
                "Pointer arithmetic is tied to the data type size. Array names decay to pointers. Double pointers (**p) are used for matrix allocations.",
                "Board note on pointer arithmetic and memory.",
                BoardScan.ReviewStatus.NEW,
                ["important", "review"],
                now - timedelta(days=2),
            ),
            (
                "C Programming Fundamentals",
                "Data Structures",
                "Overview",
                "Oct 15 - Linked Lists",
                "A linked list node contains data and a pointer to the next node. Useful for dynamic size allocations compared to arrays.",
                "Board note on linked lists creation and traversal.",
                BoardScan.ReviewStatus.NEEDS_REVIEW,
                ["important"],
                now - timedelta(days=1),
            ),
            (
                "Database Systems",
                "Transactions",
                "Overview",
                "Sept 10 - Transactions",
                "ACID means atomicity, consistency, isolation, and durability. A transaction should commit fully or roll back safely.",
                "Transaction note for ACID properties and rollback behavior.",
                BoardScan.ReviewStatus.REVIEWED,
                ["database"],
                now - timedelta(days=5),
            ),
            (
                "Database Systems",
                "Normalization",
                "Overview",
                "Sept 09 - Normalization",
                "Normalization reduces duplicated data. 3NF removes transitive dependency from non-key attributes.",
                "Normalization note about 3NF and reducing redundancy.",
                BoardScan.ReviewStatus.NEEDS_REVIEW,
                ["database", "review"],
                now - timedelta(days=6),
            ),
            (
                "Database Systems",
                "SQL Fundamentals",
                "Joins",
                "Sept 08 - SQL Joins",
                "INNER JOIN returns matching rows. LEFT JOIN keeps all rows from the left table and fills missing right-side data with null.",
                "SQL joins note for DB204 lab review.",
                BoardScan.ReviewStatus.MASTERED,
                ["database", "important"],
                now - timedelta(days=7),
            ),
            (
                "Native Android Development",
                "UI Layouts",
                "ConstraintLayout Basics",
                "UI Layout Sketch",
                "ConstraintLayout helps flatten the view hierarchy and can improve measure and layout performance.",
                "Key layout note for module reader UI.",
                BoardScan.ReviewStatus.MASTERED,
                ["android"],
                now - timedelta(days=4),
            ),
        ]

        scans = {}
        for subject_title, module_title, chapter_title, title, raw_text, summary, status, tag_names, created_at in scan_data:
            module = modules[module_title]
            chapter = chapters.get((module_title, chapter_title))
            scan, _ = BoardScan.objects.update_or_create(
                owner=user,
                subject=subjects[subject_title],
                module=module,
                chapter=chapter,
                raw_ocr_text=raw_text,
                defaults={
                    "cleaned_text": raw_text,
                    "summary": summary,
                    "review_status": status,
                },
            )
            scan.tags.set([tags[name] for name in tag_names])
            BoardScan.objects.filter(pk=scan.pk).update(created_at=created_at, updated_at=created_at)
            scans[title] = scan
        return scans

    def _create_reading_progress(self, user, modules, chapters):
        progress_data = [
            ("Intro to Android UI", "UI Patterns", 45, "Last opened 2 hours ago", ReadingProgress.Status.READING),
            ("SQL Fundamentals", "Basic Queries", 92, "Last opened yesterday", ReadingProgress.Status.READING),
            ("Final Prototype", "Prototype Checklist", 10, "Opened during prototype planning", ReadingProgress.Status.READING),
        ]

        for module_title, chapter_title, progress, position, status in progress_data:
            module = modules[module_title]
            chapter = chapters.get((module_title, chapter_title))
            ReadingProgress.objects.update_or_create(
                owner=user,
                module=module,
                chapter=chapter,
                defaults={
                    "progress_percentage": progress,
                    "last_position": position,
                    "status": status,
                },
            )

    def _create_study_tools(self, user, modules, chapters, scans, now):
        ui_layouts = modules["UI Layouts"]
        constraint_chapter = chapters[("UI Layouts", "ConstraintLayout Basics")]
        architecture_scan = scans["Sept 14 - System Architecture"]

        Summary.objects.update_or_create(
            owner=user,
            module=ui_layouts,
            chapter=constraint_chapter,
            source_type=SourceType.CHAPTER,
            defaults={
                "content": (
                    "ConstraintLayout helps build flexible Android interfaces. It reduces deeply nested layouts, "
                    "defines relationships between UI elements, and improves readability and performance."
                ),
                "is_ai_generated": True,
            },
        )

        flashcards = [
            (
                "What is the main advantage of ConstraintLayout?",
                "It can flatten the view hierarchy by positioning views through constraints.",
                Difficulty.EASY,
            ),
            (
                "What does state hoisting mean?",
                "Moving state to a parent composable so child composables receive state and events.",
                Difficulty.MEDIUM,
            ),
            (
                "Why use a repository in Android architecture?",
                "It separates data access from UI logic and coordinates local or remote data sources.",
                Difficulty.MEDIUM,
            ),
        ]
        for question, answer, difficulty in flashcards:
            Flashcard.objects.update_or_create(
                owner=user,
                module=ui_layouts,
                chapter=constraint_chapter,
                question=question,
                defaults={
                    "answer": answer,
                    "source_type": SourceType.CHAPTER,
                    "is_ai_generated": True,
                    "difficulty": difficulty,
                },
            )

        quiz, _ = Quiz.objects.update_or_create(
            owner=user,
            module=ui_layouts,
            chapter=constraint_chapter,
            title="Quiz 3: UI Patterns",
            defaults={
                "description": "Practice quiz about UI layout and Android architecture.",
                "source_type": SourceType.CHAPTER,
                "is_ai_generated": True,
            },
        )
        questions = [
            (
                1,
                "Which layout approach helps reduce deeply nested view hierarchies?",
                ["ConstraintLayout", "Nested LinearLayouts only", "Hardcoded margins", "Duplicate screens"],
                "ConstraintLayout",
                "ConstraintLayout positions views using relationships instead of deep nesting.",
            ),
            (
                2,
                "Which layer should handle data sources in a simple Android architecture?",
                ["Composable", "Repository", "Theme", "IconButton"],
                "Repository",
                "A repository coordinates data access and keeps UI logic cleaner.",
            ),
        ]
        for order, question, choices, answer, explanation in questions:
            QuizQuestion.objects.update_or_create(
                quiz=quiz,
                order=order,
                defaults={
                    "question": question,
                    "question_type": QuizQuestion.QuestionType.MULTIPLE_CHOICE,
                    "choices": choices,
                    "correct_answer": answer,
                    "explanation": explanation,
                },
            )

        QuizAttempt.objects.update_or_create(
            owner=user,
            quiz=quiz,
            defaults={
                "score": 2,
                "total_questions": 2,
                "answers": {"1": "ConstraintLayout", "2": "Repository"},
                "completed_at": now - timedelta(hours=6),
            },
        )

        Summary.objects.update_or_create(
            owner=user,
            board_scan=architecture_scan,
            source_type=SourceType.BOARD_SCAN,
            defaults={
                "content": "The board note connects ViewModel, repository, and backend APIs as the main StudyLens data flow.",
                "is_ai_generated": True,
            },
        )

    def _create_tutor_session(self, user, modules, chapters):
        module = modules["UI Layouts"]
        chapter = chapters[("UI Layouts", "ConstraintLayout Basics")]
        session, _ = TutorSession.objects.update_or_create(
            owner=user,
            module=module,
            chapter=chapter,
            title="UI Layouts Tutor",
            defaults={
                "status": TutorSession.Status.IN_PROGRESS,
                "clear_answers_count": 1,
                "target_clear_answers": 3,
            },
        )
        TutorMessage.objects.update_or_create(
            session=session,
            role=TutorMessage.Role.ASSISTANT,
            content="Explain why ConstraintLayout can improve performance compared with deeply nested layouts.",
            defaults={"clarity_result": ""},
        )
