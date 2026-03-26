# Personalized On-Device Knowledge Graph App
This repository contains the official implementation of the Knowledge Graph App (KG App), a platform for privacy-preserving personalization of on-device Large Language Models (LLMs).
The project addresses the unique research challenge of incorporating multimodal sensing data, including GPS-based device location and calendar events, into resource-constrained smartphone environments. By building a local Knowledge Graph (KG), the system provides a structured, up-to-date memory that improves response accuracy while ensuring sensitive personal data never leaves the device.

## System Overview
The ```Knowledge Graph App``` acts as a local intelligence layer that extracts, structures, and manages personal user data.

## Key Features
- **Multimodal Data Integration:** Leverages realistic calendar events synced with the device's native app and real-time user location data.
- **On-Device Knowledge Graph:** Extracts "Knowledge Triples" where nodes represent users and events (location, time, date) and edges represent their relationships.
- **Privacy-First Architecture:** Converts geographic data (latitude/longitude) into human-readable text locally using the Android Geocoder class, eliminating privacy risks associated with cloud-based LLM providers.
- **Dynamic Embedding Generation:** Consolidates triples into a unified knowledge store using the ```all-MiniLM-L6-v2``` sentence transformer, converted to ONNX format for efficient on-device execution.
- **Resource Optimization:** Automatically avoids updates when no change in user location is detected to prevent duplicate entries and conserve storage and computation on embedded architectures.

## Repository Structure
The project is implemented as an Android platform using Kotlin. The core logic is located within the [path to main files](app/src/main/java/com/example/knowledgegraph) directory:

### Core Files
- [KnowledgeBase.kt](app/src/main/java/com/example/knowledgegraph/KnowledgeBase.kt): Manages the local knowledge store and the generation of knowledge triples.
- [EmbeddingModel.kt](app/src/main/java/com/example/knowledgegraph/EmbeddingModel.kt): Handles the generation of embeddings using sentence transformers to represent triples in a unified space.
- [LocationViewModel.kt](app/src/main/java/com/example/knowledgegraph/LocationViewModel.kt): Manages real-time GPS sensing data and Geocoder integration to convert coordinates to human-readable text.
- [KGContentProvider.kt](app/src/main/java/com/example/knowledgegraph/KGContentProvider.kt): Facilitates data access and the extraction of multimodal triples from sensitive information.
- [DataStoreManager.kt](app/src/main/java/com/example/knowledgegraph/DataStoreManager.kt): Handles persistent storage of user preferences and metadata for the knowledge graph.
- [TokenizerLoader.kt](app/src/main/java/com/example/knowledgegraph/TokenizerLoader.kt): Manages the loading of tokenizers required for the on-device embedding model to process text.

### UI Files
- [HomeScreen.kt](app/src/main/java/com/example/knowledgegraph/HomeScreen.kt): The primary dashboard for managing the Knowledge Graph (see Fig. 5).
- [LoginScreen.kt](app/src/main/java/com/example/knowledgegraph/LoginScreen.kt) and SignUpScreen.kt: Secure user authentication modules.

## Getting Started

### Prerequisites
- Android Studio: Primary development environment for the Kotlin-based app.
- Google Play Services Location API: Required to obtain device coordinates.
- Hugging Face Sentence Transformers: Uses the all-MiniLM-L6-v2 model for embedding generation.

### Core Environment Requirements
- Rust: 1.87.0
- Android NDK: 27.0.11718014
- OpenJDK: 21.0.6

## Usage and Deployment 
Based on the system design and implementation details, here are the steps to set up and run the Knowledge Graph App on your device.
### Prerequisites and Device Access
To enable multimodal personalization, the app requires specific permissions to access sensitive on-device data:
- Calendar Access: Ensure the device is synced with Google Calendar. The app extracts events directly from the native calendar app to build the knowledge base.
- Location Access: Grant "Precise Location" permissions. The app uses the Google Play Services Location API to retrieve latitude and longitude, which are then converted to human-readable text via the Geocoder class.
### Setting Up the Embedding Model
The app uses the ```all-MiniLM-L6-v2``` sentence transformer to convert text triples into vector embeddings.
- Source: The model is sourced from [Hugging Face](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2).
- Format: It must be converted to the ONNX format to run on Android ([GitHub](https://github.com/shubham0204/Sentence-Embeddings-Android)).
- Implementation: The logic for loading and running this model is handled in [EmbeddingModel.kt](app/src/main/java/com/example/knowledgegraph/EmbeddingModel.kt) and [TokenizerLoader.kt](app/src/main/java/com/example/knowledgegraph/TokenizerLoader.kt) using the Sentence-Embeddings-Android library.
  
### Building and Running the App
The project is built as an Android platform using Kotlin.
- Connect Device: Attach your smartphone (Device used: Samsung Galaxy S23) to your computer via USB and enable USB Debugging in Developer Options.
- Environment Sync: Open the project in Android Studio and ensure the build.gradle.kts files are synced.
- Core Dependencies: Verify that Rust (1.87.0) and Android NDK (27.0.11718014) are installed, as they are prerequisites for the underlying deployment framework.
- Deploy: Click Run 'app' in Android Studio to install the APK onto your connected phone.

### Accessing Data and Triples
Once the app is running, you can monitor the generated data through the Knowledge Graph App interface (HomeScreen.kt):
- File Export: The structured triples are saved locally to a file named Knowledge_graph.csv.
- Vector Store: The generated embeddings are stored in Knowledge_graph.vec for use in the RAG-based response generation.
- Dataset Location: All generated KG Triples and Location Triples generated for our evaluation are within the [Evaluation Data](dataset-generated/) folder in this repository. This also includes the user query and golden answer.

# Contributors
- Deeksha Prahlad (dprahlad@asu.edu), Ph.D. student at Arizona State University 
- Hokeun Kim (hokeun@asu.edu, [website](https://hokeun.github.io/)), Assistant Professor at Arizona State University
