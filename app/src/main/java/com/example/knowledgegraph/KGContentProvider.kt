package com.example.knowledgegraph

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileNotFoundException

class KGContentProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "com.example.knowledgegraph.kgprovider"
        private const val PATH_KG = "knowledge_graph"
        private const val PATH_VEC = "knowledge_graph_vec"
        private const val CODE_KG = 1
        private const val CODE_VEC = 2

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_KG")
        val CONTENT_URI_VEC: Uri = Uri.parse("content://$AUTHORITY/$PATH_VEC")

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_KG, CODE_KG)
            addURI(AUTHORITY, PATH_VEC, CODE_VEC)
        }
    }

    override fun onCreate(): Boolean {
        Log.d("KGContentProvider", "Provider initialized")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: return null

        return when (uriMatcher.match(uri)) {
            CODE_KG -> {
                val file = File(context.getExternalFilesDir(null), "Knowledge_graph.csv")
                if (!file.exists()) {
                    Log.e("KGContentProvider", "CSV file not found: ${file.absolutePath}")
                    return null
                }

                val cursor = MatrixCursor(arrayOf("subject", "predicate", "object"))
                file.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.size == 3) {
                        cursor.addRow(arrayOf(parts[0].trim(), parts[1].trim(), parts[2].trim()))
                    }
                }

                Log.d("KGContentProvider", "Query completed for CSV, rows=${cursor.count}")
                cursor
            }

            CODE_VEC -> {
                // .vec is binary/numeric data — not suitable for query(), return null
                Log.w("KGContentProvider", "Query on .vec not supported")
                null
            }

            else -> {
                Log.e("KGContentProvider", "Unknown URI: $uri")
                null
            }
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_KG -> "vnd.android.cursor.dir/vnd.com.example.knowledgegraph.csv"
            CODE_VEC -> "application/octet-stream"
            else -> null
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val context = context ?: return null

        val file = when (uriMatcher.match(uri)) {
            CODE_KG -> File(context.getExternalFilesDir(null), "Knowledge_graph.csv")
            CODE_VEC -> File(context.getExternalFilesDir(null), "Knowledge_graph.vec")
            else -> throw FileNotFoundException("Unknown URI: $uri")
        }

        if (!file.exists()) {
            throw FileNotFoundException("File not found: ${file.absolutePath}")
        }

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}