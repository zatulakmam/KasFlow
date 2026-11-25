package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Transaction
import com.example.model.TransactionCategory
import com.example.model.TransactionType
import com.example.util.yearMonthFromMillis
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TransactionRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        require(!uid.isNullOrBlank()) {
            "Pengguna harus masuk dengan Google terlebih dahulu."
        }
        return uid
    }

    // READ (Real-time stream)
    fun observeTransactions(userId: String): Flow<List<Transaction>> = flow {
        require(userId.isNotBlank()) { "User ID tidak boleh kosong." }
        val activeUid = requireUserId()
        check(activeUid == userId) { "Sesi pengguna berubah; menghentikan listener." }

        val path = "users/$userId/transactions"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Transaction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<Transaction> { it.date }
                            .thenByDescending { it.createdAt ?: Timestamp(0, 0) }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // READ (One-shot list)
    suspend fun getTransactions(userId: String): Result<List<Transaction>> {
        val path = "users/$userId/transactions"
        return try {
            val activeUid = requireUserId()
            check(activeUid == userId) { "Akses lintas pengguna ditolak." }
            val snapshot = db.collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            val items = snapshot.toObjects(
                Transaction::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    // READ (Single document by ID)
    suspend fun getTransactionById(userId: String, transactionId: String): Result<Transaction?> {
        val path = "users/$userId/transactions/$transactionId"
        return try {
            val activeUid = requireUserId()
            check(activeUid == userId) { "Akses lintas pengguna ditolak." }
            require(transactionId.isNotBlank()) { "ID transaksi tidak valid." }
            val snapshot = db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId)
                .get()
                .await()
            val item = snapshot.toObject(
                Transaction::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    // CREATE (Model-based)
    suspend fun createTransaction(transaction: Transaction): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/transactions"
        return try {
            val uid = requireUserId()
            if (transaction.userId.isNotBlank()) {
                check(transaction.userId == uid) { "userId tidak sesuai dengan pengguna aktif." }
            }

            val parsedType = TransactionType.fromRaw(transaction.type)
            val parsedCategory = TransactionCategory.fromRaw(transaction.category, parsedType)
            val effectiveDate = if (transaction.date > 0L) transaction.date else System.currentTimeMillis()
            val cleanTitle = transaction.title.trim().ifEmpty { parsedCategory.label }.take(100)
            val cleanNote = transaction.note.trim()
            val ym = transaction.yearMonth.takeIf { it.matches(Regex("^[0-9]{4}-(0[1-9]|1[0-2])$")) }
                ?: yearMonthFromMillis(effectiveDate)

            require(cleanTitle.isNotEmpty() && cleanTitle.length <= 100) {
                "Judul transaksi harus 1–100 karakter."
            }
            require(transaction.amount in 1L..999_999_999_999L) {
                "Nominal harus lebih dari 0 dan maksimal 999 miliar."
            }
            require(cleanNote.length <= 250) {
                "Catatan maksimal 250 karakter."
            }

            val docRef = if (transaction.id.isNotBlank()) {
                db.collection("users").document(uid).collection("transactions").document(transaction.id)
            } else {
                db.collection("users").document(uid).collection("transactions").document()
            }
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "amount" to transaction.amount,
                "type" to parsedType.name,
                "category" to parsedCategory.name,
                "date" to effectiveDate,
                "title" to cleanTitle,
                "note" to cleanNote,
                "yearMonth" to ym,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    // CREATE (UI convenience overload)
    suspend fun addTransaction(
        title: String,
        type: TransactionType,
        category: TransactionCategory,
        amount: Long,
        note: String,
        date: Long
    ): Result<Unit> {
        val uid = auth.currentUser?.uid.orEmpty()
        return createTransaction(
            Transaction(
                userId = uid,
                amount = amount,
                type = type.name,
                category = category.name,
                date = date,
                title = title,
                note = note,
                yearMonth = if (date > 0L) yearMonthFromMillis(date) else ""
            )
        ).map { }
    }

    // UPDATE (Model-based partial update preserving immutable userId & createdAt)
    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return updateTransaction(
            transactionId = transaction.id,
            title = transaction.title.ifBlank { transaction.parsedCategory.label },
            type = transaction.parsedType,
            category = transaction.parsedCategory,
            amount = transaction.amount,
            note = transaction.note,
            date = transaction.date
        )
    }

    // UPDATE (Field-based partial update)
    suspend fun updateTransaction(
        transactionId: String,
        title: String,
        type: TransactionType,
        category: TransactionCategory,
        amount: Long,
        note: String,
        date: Long
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/transactions/$transactionId"
        return try {
            val uid = requireUserId()
            require(transactionId.isNotBlank()) { "ID transaksi tidak valid." }

            val cleanTitle = title.trim()
            val cleanNote = note.trim()
            val ym = yearMonthFromMillis(date)

            require(cleanTitle.isNotEmpty() && cleanTitle.length <= 100) {
                "Judul transaksi harus 1–100 karakter."
            }
            require(amount in 1L..999_999_999_999L) {
                "Nominal harus lebih dari 0 dan maksimal 999 miliar."
            }
            require(cleanNote.length <= 250) {
                "Catatan maksimal 250 karakter."
            }
            require(date > 0L) {
                "Tanggal transaksi tidak valid."
            }

            val docRef = db.collection("users")
                .document(uid)
                .collection("transactions")
                .document(transactionId)
            docPath = docRef.path

            val updates = mapOf(
                "amount" to amount,
                "type" to type.name,
                "category" to category.name,
                "date" to date,
                "title" to cleanTitle,
                "note" to cleanNote,
                "yearMonth" to ym,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    // DELETE
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/transactions/$transactionId"
        return try {
            val uid = requireUserId()
            require(transactionId.isNotBlank()) { "ID transaksi tidak valid." }
            val docRef = db.collection("users")
                .document(uid)
                .collection("transactions")
                .document(transactionId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
