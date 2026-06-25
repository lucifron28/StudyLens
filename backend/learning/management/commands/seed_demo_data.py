from __future__ import annotations

from datetime import timedelta
from textwrap import dedent

from django.contrib.auth import get_user_model
from django.core.management.base import BaseCommand
from django.utils import timezone

from ai_services.models import TutorMessage, TutorSession
from learning.models import BoardScan, Chapter, Module, ReadingProgress, StudyTask, Subject, Tag
from studytools.models import Difficulty, Flashcard, Quiz, QuizAttempt, QuizQuestion, SourceType, Summary


def markdown(text: str) -> str:
    return dedent(text).strip()


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
                (
                    "C Basics and Control Flow",
                    "Program structure, variables, operators, decisions, loops, and basic console input.",
                    True,
                ),
                (
                    "Pointers and Memory",
                    "Addresses, pointer variables, arrays, pointer arithmetic, dynamic allocation, and memory safety.",
                    True,
                ),
                (
                    "Functions and Files",
                    "Function design, parameter passing, scope, header files, and basic file input/output.",
                    False,
                ),
                (
                    "Data Structures",
                    "Structs, linked lists, stacks, queues, and memory-aware data structure design in C.",
                    False,
                ),
            ],
        }

        module_bodies = {
            "C Basics and Control Flow": markdown(
                """
                # C Basics and Control Flow

                C is a compiled, procedural programming language. A C program is usually written in one or more
                `.c` source files, compiled into machine code, and then executed by the operating system. The
                language gives the programmer direct control over memory and data representation, which makes it
                powerful but less forgiving than higher-level languages.

                ## Program Structure

                Most beginner programs start with `#include <stdio.h>` and a `main` function. The `#include`
                line asks the preprocessor to copy declarations from the standard input/output header. The
                `main` function is the program entry point. A return value of `0` usually means the program
                finished successfully.

                ```c
                #include <stdio.h>

                int main(void) {
                    printf("Hello, StudyLens!\\n");
                    return 0;
                }
                ```

                ## Variables and Types

                A variable reserves storage for a value. Common primitive types include `int`, `float`, `double`,
                and `char`. C does not automatically protect you from every conversion problem, so it is important
                to choose a type that matches the data. For example, use `double` for decimal calculations that
                need more precision, and use `int` for whole-number counters.

                ## Decisions and Loops

                `if`, `else if`, and `else` control which block executes. `for` loops are often used when the
                number of repetitions is known, while `while` loops are useful when repetition depends on a
                condition. A common beginner mistake is writing loop conditions that never become false.

                ## Key Takeaways

                - C programs run from `main`.
                - Variables must be declared with a type.
                - Format specifiers such as `%d`, `%f`, and `%c` must match the value being printed.
                - Loops need clear initialization, condition checks, and updates.
                - Small test cases make logic errors easier to find.
                """
            ),
            "Pointers and Memory": markdown(
                """
                # Pointers and Memory

                A pointer is a variable that stores the address of another value. This is one of the most important
                ideas in C because arrays, strings, dynamic memory, and many data structures rely on addresses.
                Instead of only asking "what is the value?", C often asks "where is the value stored?"

                ## Address and Dereference

                The address-of operator `&` gets the memory address of a variable. The dereference operator `*`
                accesses the value stored at an address.

                ```c
                int score = 95;
                int *ptr = &score;
                printf("%d\\n", *ptr); // prints 95
                ```

                `ptr` does not store `95`; it stores the address where `score` lives. Dereferencing `ptr` reads or
                writes the value at that address.

                ## Arrays and Pointer Arithmetic

                In many expressions, an array name decays into a pointer to its first element. If `numbers` is an
                array of integers, then `numbers + 1` points to the next integer, not the next byte. Pointer
                arithmetic scales by the size of the pointed type.

                ## Dynamic Memory

                `malloc` requests memory from the heap. `free` returns that memory when it is no longer needed.
                Every successful allocation should have a matching `free`. Forgetting to free memory creates a
                memory leak. Using memory after freeing it is called a use-after-free bug.

                ```c
                int *items = malloc(5 * sizeof(int));
                if (items == NULL) {
                    return 1;
                }
                items[0] = 10;
                free(items);
                items = NULL;
                ```

                ## Common Bugs

                - Dereferencing an uninitialized pointer.
                - Writing beyond the end of an array.
                - Calling `free` twice on the same pointer.
                - Returning the address of a local variable.
                - Forgetting to check whether `malloc` returned `NULL`.
                """
            ),
            "Functions and Files": markdown(
                """
                # Functions and Files

                Functions let a C program be divided into smaller parts. A good function performs one clear task,
                has a meaningful name, receives only the data it needs, and returns a useful result. This makes
                programs easier to test and easier to explain during a defense or code review.

                ## Function Design

                A function declaration tells the compiler the function name, return type, and parameter types.
                A function definition contains the actual body.

                ```c
                int add(int left, int right);

                int add(int left, int right) {
                    return left + right;
                }
                ```

                C passes function arguments by value. If a function needs to modify the caller's variable, pass a
                pointer to that variable.

                ## Scope and Header Files

                A variable declared inside a function has local scope. It exists only while that function is
                running. Header files usually contain declarations, constants, and shared struct definitions.
                Source files contain implementation details.

                ## File Input and Output

                The `FILE *` type represents an open file. Use `fopen` to open a file, `fprintf` or `fscanf` to
                write or read formatted data, and `fclose` when finished. Always check if `fopen` returns `NULL`,
                because the file might not exist or the program might not have permission.

                ```c
                FILE *file = fopen("scores.txt", "r");
                if (file == NULL) {
                    printf("Could not open file.\\n");
                    return 1;
                }
                fclose(file);
                ```

                ## Key Takeaways

                - Functions reduce repeated code.
                - Use pointers when a function must update caller-owned data.
                - Header files describe shared interfaces.
                - Always close files after use.
                - File operations can fail, so check return values.
                """
            ),
            "Data Structures": markdown(
                """
                # Data Structures in C

                A data structure organizes values so a program can store, search, insert, and remove data
                efficiently. In C, data structures are often built from `struct` types and pointers. Because C does
                not manage memory automatically, the programmer must also decide when each node or record is
                allocated and freed.

                ## Structs

                A `struct` groups related fields into one custom type.

                ```c
                typedef struct {
                    int id;
                    char name[50];
                    double grade;
                } Student;
                ```

                Structs are useful for records such as students, products, accounts, and nodes.

                ## Linked Lists

                A linked list stores each item in a node. Each node contains data and a pointer to the next node.
                Unlike arrays, linked lists can grow one node at a time. The tradeoff is that linked lists do not
                support direct indexing; to reach an item, the program must follow links from the head node.

                ## Stacks and Queues

                A stack follows last-in, first-out behavior. It is useful for undo actions, expression parsing, and
                function call tracking. A queue follows first-in, first-out behavior. It is useful for scheduling,
                print jobs, and request processing.

                ## Memory Discipline

                Every dynamically allocated node must eventually be freed. When deleting from a linked structure,
                update the surrounding links before freeing the removed node. Losing the only pointer to allocated
                memory causes a leak.

                ## Key Takeaways

                - Structs model records.
                - Linked lists are flexible but require pointer traversal.
                - Stacks use push and pop.
                - Queues use enqueue and dequeue.
                - Dynamic structures require careful allocation and cleanup.
                """
            ),
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
            "C Basics and Control Flow": [
                (
                    "Overview",
                    markdown(
                        """
                        C is a low-level procedural language used for systems programming, embedded software,
                        operating systems, and performance-sensitive applications. A C program is compiled before
                        it runs, so syntax and type mistakes are usually caught by the compiler.

                        The basic workflow is: write source code, compile it, run the executable, then test the
                        result. A beginner should learn to read compiler messages carefully because they often
                        point directly to missing semicolons, undeclared variables, or mismatched types.
                        """
                    ),
                ),
                (
                    "Variables and Operators",
                    markdown(
                        """
                        Variables store values in memory. Common types include `int` for whole numbers, `float`
                        and `double` for decimal values, and `char` for single characters. Each type has a size,
                        a range, and a matching format specifier when printed with `printf`.

                        Operators perform calculations and comparisons. Arithmetic operators include `+`, `-`,
                        `*`, `/`, and `%`. Relational operators such as `==`, `!=`, `<`, and `>` create conditions.
                        Logical operators such as `&&`, `||`, and `!` combine conditions.

                        ```c
                        int age = 20;
                        if (age >= 18 && age <= 25) {
                            printf("College age range\\n");
                        }
                        ```
                        """
                    ),
                ),
                (
                    "Control Flow and Loops",
                    markdown(
                        """
                        Control flow decides which statements execute. Use `if` when there are choices, `switch`
                        when one value is compared against several cases, and loops when work must repeat.

                        A `for` loop is best when the counter is clear. A `while` loop is best when the program
                        repeats until an input or state changes.

                        ```c
                        for (int i = 1; i <= 5; i++) {
                            printf("%d\\n", i);
                        }
                        ```

                        Common loop bugs include off-by-one errors, missing updates, and conditions that never
                        become false. Tracing the value of the loop variable by hand is a useful debugging habit.
                        """
                    ),
                ),
            ],
            "Pointers and Memory": [
                (
                    "Overview",
                    markdown(
                        """
                        Pointers are variables that store memory addresses. They allow a program to work directly
                        with where data is stored. This is useful for arrays, dynamic memory, linked lists, and
                        functions that need to modify caller-owned values.

                        A pointer has a type. An `int *` points to an integer. A `char *` points to a character.
                        The type matters because pointer arithmetic depends on the size of the pointed value.
                        """
                    ),
                ),
                (
                    "Pointer Fundamentals",
                    markdown(
                        """
                        The address-of operator `&` gets the address of a variable. The dereference operator `*`
                        accesses the value stored at an address.

                        ```c
                        int number = 42;
                        int *p = &number;
                        *p = 50;
                        ```

                        After this code, `number` becomes `50` because `p` points to `number`. This is why pointers
                        can be used to update values inside helper functions.

                        A pointer should point to valid memory before it is dereferenced. Dereferencing an
                        uninitialized pointer can crash the program or corrupt data.
                        """
                    ),
                ),
                (
                    "Arrays and Pointer Arithmetic",
                    markdown(
                        """
                        Arrays and pointers are closely related in C. In many expressions, an array name behaves
                        like a pointer to the first element.

                        ```c
                        int values[3] = {10, 20, 30};
                        printf("%d\\n", *(values + 1)); // prints 20
                        ```

                        Pointer arithmetic moves by element size. If an integer uses four bytes, `values + 1`
                        moves four bytes forward. This is why pointer type is important.

                        C does not automatically check array bounds. Writing to `values[3]` in a three-element
                        array is invalid and may overwrite unrelated memory.
                        """
                    ),
                ),
                (
                    "Dynamic Memory",
                    markdown(
                        """
                        Dynamic memory is requested from the heap with `malloc`, `calloc`, or `realloc`. It must
                        be released with `free`.

                        ```c
                        int *scores = malloc(count * sizeof(int));
                        if (scores == NULL) {
                            return 1;
                        }

                        free(scores);
                        scores = NULL;
                        ```

                        Use `sizeof(*scores)` or `sizeof(int)` to allocate the correct number of bytes. Always
                        check for `NULL` before using allocated memory. After freeing a pointer, setting it to
                        `NULL` helps avoid accidental reuse.
                        """
                    ),
                ),
            ],
            "Functions and Files": [
                (
                    "Overview",
                    markdown(
                        """
                        Functions organize a program into named actions. Instead of writing one long `main`
                        function, a C program should separate input, processing, output, and cleanup into smaller
                        functions.

                        File handling lets a C program keep data after the program exits. A file can store scores,
                        logs, records, configuration values, or generated reports.
                        """
                    ),
                ),
                (
                    "Parameter Passing",
                    markdown(
                        """
                        C passes arguments by value. The function receives a copy of the argument. If the function
                        changes the parameter, the caller's original variable does not change.

                        To modify caller data, pass a pointer:

                        ```c
                        void reset(int *value) {
                            *value = 0;
                        }
                        ```

                        This pattern is common for functions that need to return more than one result, update a
                        counter, or fill an array.
                        """
                    ),
                ),
                (
                    "Header Files",
                    markdown(
                        """
                        Header files usually contain function prototypes, constants, and shared type definitions.
                        Source files contain function implementations. This separation makes larger programs easier
                        to maintain.

                        A simple project might have `student.h` for declarations and `student.c` for function
                        bodies. The `main.c` file includes the header and calls the functions without needing to
                        know every implementation detail.
                        """
                    ),
                ),
                (
                    "File Input and Output",
                    markdown(
                        """
                        Use `fopen` to open a file. The mode controls whether the program reads, writes, or appends.
                        `r` reads, `w` writes and replaces existing content, and `a` appends to the end.

                        ```c
                        FILE *file = fopen("students.txt", "w");
                        if (file == NULL) {
                            printf("Unable to open file.\\n");
                            return 1;
                        }
                        fprintf(file, "Ana 92\\n");
                        fclose(file);
                        ```

                        Always close files with `fclose`. Check return values because file operations can fail.
                        """
                    ),
                ),
            ],
            "Data Structures": [
                (
                    "Overview",
                    markdown(
                        """
                        Data structures describe how values are organized in memory. In C, the programmer often
                        builds structures manually using `struct`, arrays, and pointers.

                        The correct structure depends on the operation. Arrays are simple and fast for indexing.
                        Linked lists are flexible for insertion and deletion. Stacks and queues enforce specific
                        access rules.
                        """
                    ),
                ),
                (
                    "Structs and Records",
                    markdown(
                        """
                        A `struct` combines multiple fields into one custom type. It is useful when one record has
                        several related values.

                        ```c
                        typedef struct {
                            int id;
                            char name[50];
                            double grade;
                        } Student;
                        ```

                        Struct variables can be stored in arrays, passed to functions, or used inside linked list
                        nodes. Use the dot operator for normal struct variables and the arrow operator for struct
                        pointers.
                        """
                    ),
                ),
                (
                    "Linked Lists",
                    markdown(
                        """
                        A linked list node stores data and a pointer to the next node.

                        ```c
                        typedef struct Node {
                            int value;
                            struct Node *next;
                        } Node;
                        ```

                        The first node is called the head. To traverse the list, start at the head and follow
                        `next` until it becomes `NULL`. Inserting at the beginning is usually simple: allocate a new
                        node, point it to the old head, then update the head pointer.

                        Linked lists are flexible, but they require careful memory management. Every allocated node
                        should be freed when the list is destroyed.
                        """
                    ),
                ),
                (
                    "Stacks and Queues",
                    markdown(
                        """
                        A stack follows last-in, first-out behavior. The main operations are `push`, `pop`, and
                        `peek`. A queue follows first-in, first-out behavior. The main operations are `enqueue`,
                        `dequeue`, and `front`.

                        Stacks can be implemented with arrays or linked lists. Queues often keep both front and
                        rear pointers so insertion and removal stay efficient.

                        Practical uses:

                        - Stack: undo feature, expression checking, function calls.
                        - Queue: task scheduling, print queue, request processing.
                        """
                    ),
                ),
                (
                    "Debugging Memory Bugs",
                    markdown(
                        """
                        Dynamic data structures can fail in subtle ways. Common bugs include losing the head
                        pointer, skipping nodes during traversal, forgetting to link a new node, and freeing memory
                        too early.

                        A good debugging approach is to draw the nodes and arrows before coding. After every insert
                        or delete operation, verify which pointer changes first. When deleting a node, save the next
                        pointer before freeing the current node.
                        """
                    ),
                ),
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
                        "markdown_content": module_bodies.get(title, f"# {title}\n\n{description}"),
                        "extracted_text": module_bodies.get(title, description),
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
                StudyTask.TaskType.NOTE,
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
