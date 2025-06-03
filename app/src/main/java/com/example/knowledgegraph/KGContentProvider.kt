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
        private const val CODE_KG = 1

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_KG")

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_KG, CODE_KG)
        }
    }

    override fun onCreate(): Boolean {
        Log.d("KGContentProvider", "Provider initialized")
        return true
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {
        val context = context ?: return null

        if (uriMatcher.match(uri) != CODE_KG) {
            Log.e("KGContentProvider", "Unknown URI: $uri")
            return null
        }

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

        Log.d("KGContentProvider", "Query completed, rows=${cursor.count}")
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_KG -> "vnd.android.cursor.dir/vnd.com.example.knowledgegraph"
            else -> null
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val context = context ?: return null
        if (uriMatcher.match(uri) != CODE_KG) throw FileNotFoundException("Unknown URI: $uri")

        val file = File(context.getExternalFilesDir(null), "Knowledge_graph.csv")
        if (!file.exists()) throw FileNotFoundException("File not found: ${file.absolutePath}")

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?) = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
}