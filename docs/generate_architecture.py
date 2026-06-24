"""Render the StudyLens architecture with Mingrammer Diagrams.

Prerequisites on Windows:
    winget install Graphviz.Graphviz
    uv run --with diagrams python generate_architecture.py
"""

import os
from pathlib import Path

from diagrams import Cluster, Diagram, Edge
from diagrams.gcp.ml import AIPlatform, VisionAPI
from diagrams.generic.os import Android
from diagrams.generic.storage import Storage
from diagrams.onprem.client import Client
from diagrams.onprem.compute import Server
from diagrams.onprem.container import Docker
from diagrams.onprem.database import PostgreSQL
from diagrams.programming.framework import Django
from diagrams.programming.language import Kotlin, Python


GRAPHVIZ_BIN = Path(r"C:\Program Files\Graphviz\bin")
if GRAPHVIZ_BIN.exists():
    os.environ["PATH"] = f"{GRAPHVIZ_BIN}{os.pathsep}{os.environ['PATH']}"


with Diagram(
    "StudyLens Architecture",
    show=False,
    filename="studylens_architecture",
    outformat="png",
    direction="LR",
    graph_attr={
        "pad": "0.3",
        "nodesep": "0.7",
        "ranksep": "0.8",
        "splines": "spline",
        "fontsize": "24",
        "fontname": "Arial",
    },
    node_attr={"fontsize": "14", "fontname": "Arial"},
    edge_attr={"fontname": "Arial", "fontsize": "11"},
):
    with Cluster("Android App", direction="TB"):
        compose = Kotlin("Jetpack Compose\nscreens and navigation")
        view_models = Kotlin("ViewModels\nUI state and actions")
        repositories = Kotlin("Repositories\nRetrofit and DTO mapping")
        token_store = Storage("DataStore\nJWT tokens")

        camera = Android("CameraX\nboard capture")
        cropper = Kotlin("Image cropper\npinch, drag, resize")
        ocr = VisionAPI("ML Kit OCR\nrecognized board text")

        pdf_cache = Storage("File cache\ndownloaded PDFs")
        pdf_viewer = Client("Native PdfRenderer\nmodule reader")

        compose >> view_models >> repositories
        token_store >> Edge(label="tokens") >> repositories
        camera >> cropper >> ocr >> Edge(label="OCR text") >> repositories
        repositories >> Edge(label="download") >> pdf_cache >> pdf_viewer

    with Cluster("Docker Compose Backend"):
        docker = Docker("Docker Compose\nweb and database services")
        api = Django("Django REST Framework\nJWT, learning, study tools, Swagger")
        libreoffice = Server("LibreOffice\nDOCX/PPTX to PDF")
        extractor = Python("PyMuPDF and fallback parsers\nreadable module text")
        ai_service = Django("AI Service\nsummaries, quizzes, tutor")
        database = PostgreSQL("PostgreSQL\nstudent-owned data")
        media = Storage("Media volume\nfiles and scan images")

        docker >> Edge(label="runs") >> api
        api >> Edge(label="DOCX/PPTX") >> libreoffice
        api >> Edge(label="PDF") >> extractor
        libreoffice >> Edge(label="converted PDF") >> extractor
        api >> Edge(label="uploads") >> media
        extractor >> Edge(label="extracted text") >> database
        api >> ai_service
        ai_service >> database

    with Cluster("AI Providers"):
        ollama = Server("Ollama\nqwen3:4b-instruct")
        gemini = AIPlatform("Gemini\noptional provider")

    repositories >> Edge(label="HTTPS REST\nJSON and multipart") >> api
    ai_service >> Edge(label="local default", color="purple") >> ollama
    ai_service >> Edge(label="when configured", color="purple", style="dashed") >> gemini
