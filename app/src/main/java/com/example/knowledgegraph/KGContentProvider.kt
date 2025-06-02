package com.example.knowledgegraph

// import statements
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import java.io.File
import android.util.Log

class KGContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        Log.d("KGContentProvider", "Provider initialized")
        return true
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        val file = File(context.getExternalFilesDir(null), "Knowledge_graph.csv")

        if (!file.exists()) return null

        val cursor = MatrixCursor(arrayOf("subject", "predicate", "object"))
        file.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 3) {
                cursor.addRow(arrayOf(parts[0].trim(), parts[1].trim(), parts[2].trim()))
            }
        }
        return cursor
    }

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.dir/vnd.com.example.knowledgegraph.kg"
    }
    override fun insert(uri: Uri, values: ContentValues?) = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0

}
