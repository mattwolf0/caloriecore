package app.caloriecore.data

import android.content.ContentValues
import android.database.Cursor

internal inline fun <reified T : Enum<T>> enumValue(raw: String, fallback: T): T =
    runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)

internal fun ContentValues.putNullable(column: String, maybeNumber: Double?) {
    if (maybeNumber == null) putNull(column) else put(column, maybeNumber)
}

internal fun ContentValues.putNullable(column: String, maybeId: Long?) {
    if (maybeId == null) putNull(column) else put(column, maybeId)
}

internal fun ContentValues.putNullable(column: String, maybeCount: Int?) {
    if (maybeCount == null) putNull(column) else put(column, maybeCount)
}

internal fun Cursor.string(column: String): String = getString(getColumnIndexOrThrow(column))

internal fun Cursor.int(column: String): Int = getInt(getColumnIndexOrThrow(column))

internal fun Cursor.long(column: String): Long = getLong(getColumnIndexOrThrow(column))

internal fun Cursor.double(column: String): Double = getDouble(getColumnIndexOrThrow(column))

internal fun Cursor.nullableLong(column: String): Long? {
    val index = getColumnIndexOrThrow(column)
    return if (isNull(index)) null else getLong(index)
}

internal fun Cursor.nullableInt(column: String): Int? {
    val index = getColumnIndex(column)
    return if (index < 0 || isNull(index)) null else getInt(index)
}

internal fun Cursor.nullableDouble(column: String): Double? {
    val index = getColumnIndexOrThrow(column)
    return if (isNull(index)) null else getDouble(index)
}

internal fun <T> Cursor.mapRows(mapper: Cursor.() -> T): List<T> {
    val rows = mutableListOf<T>()
    while (moveToNext()) rows += mapper()
    return rows
}
