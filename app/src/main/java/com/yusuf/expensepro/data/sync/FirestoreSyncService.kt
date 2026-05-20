package com.yusuf.expensepro.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.yusuf.expensepro.data.local.dao.*
import com.yusuf.expensepro.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val splitGroupDao: SplitGroupDao,
    private val splitMemberDao: SplitMemberDao,
    private val splitExpenseDao: SplitExpenseDao,
    private val splitShareDao: SplitShareDao,
    private val settlementDao: SettlementDao
) {
    private val uid get() = auth.currentUser?.uid
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── Public API ─────────────────────────────────────────────────────────────
    fun syncAll() {
        val u = uid ?: return
        scope.launch {
            runCatching { syncTransactions(u) }
            runCatching { syncBudgets(u) }
            runCatching { syncSplitGroups(u) }
            runCatching { syncSettlements(u) }
        }
    }

    suspend fun pullAll() {
        val u = uid ?: return
        runCatching { pullTransactions(u) }
        runCatching { pullSplitGroups(u) }
    }

    // ── Push: local → Firestore ────────────────────────────────────────────────
    private suspend fun syncTransactions(uid: String) {
        val all = transactionDao.getAllTransactions().first()
        val batch = firestore.batch()
        all.forEach { tx ->
            batch.set(
                firestore.collection("users/$uid/transactions").document(tx.id.toString()),
                mapOf("id" to tx.id, "title" to tx.title, "amount" to tx.amount,
                    "type" to tx.type, "category" to tx.category,
                    "date" to tx.date, "note" to tx.note),
                SetOptions.merge()
            )
        }
        batch.commit().await()
    }

    private suspend fun syncBudgets(uid: String) {
        val now = LocalDate.now()
        val all = budgetDao.getBudgetsForMonth(now.monthValue, now.year).first()
        val batch = firestore.batch()
        all.forEach { b ->
            batch.set(
                firestore.collection("users/$uid/budgets").document(b.id.toString()),
                mapOf("id" to b.id, "category" to b.category, "limitAmount" to b.limitAmount,
                    "month" to b.month, "year" to b.year),
                SetOptions.merge()
            )
        }
        batch.commit().await()
    }

    private suspend fun syncSplitGroups(uid: String) {
        val groups = splitGroupDao.getAllGroups().first()
        groups.forEach { group ->
            val groupRef = firestore.collection("users/$uid/split_groups").document(group.id.toString())
            groupRef.set(mapOf("id" to group.id, "name" to group.name,
                "icon" to group.icon, "createdAt" to group.createdAt), SetOptions.merge()).await()

            splitMemberDao.getMembersForGroupSync(group.id).forEach { m ->
                groupRef.collection("members").document(m.id.toString())
                    .set(mapOf("id" to m.id, "groupId" to m.groupId,
                        "name" to m.name, "isCurrentUser" to m.isCurrentUser), SetOptions.merge()).await()
            }

            splitExpenseDao.getExpensesForGroup(group.id).first().forEach { exp ->
                val expRef = groupRef.collection("expenses").document(exp.id.toString())
                expRef.set(mapOf("id" to exp.id, "groupId" to exp.groupId, "title" to exp.title,
                    "totalAmount" to exp.totalAmount, "paidByMemberId" to exp.paidByMemberId,
                    "date" to exp.date, "note" to exp.note, "category" to exp.category,
                    "splitType" to exp.splitType), SetOptions.merge()).await()

                splitShareDao.getSharesForExpenseSync(exp.id).forEach { share ->
                    expRef.collection("shares").document(share.id.toString())
                        .set(mapOf("id" to share.id, "expenseId" to share.expenseId,
                            "memberId" to share.memberId, "shareAmount" to share.shareAmount,
                            "isSettled" to share.isSettled), SetOptions.merge()).await()
                }
            }
        }
    }

    private suspend fun syncSettlements(uid: String) {
        val all = settlementDao.getAllSettlements().first()
        val batch = firestore.batch()
        all.forEach { s ->
            batch.set(
                firestore.collection("users/$uid/settlements").document(s.id.toString()),
                mapOf("id" to s.id, "groupId" to s.groupId,
                    "fromMemberId" to s.fromMemberId, "toMemberId" to s.toMemberId,
                    "amount" to s.amount, "note" to s.note, "date" to s.date,
                    "isPartial" to s.isPartial),
                SetOptions.merge()
            )
        }
        batch.commit().await()
    }

    // ── Pull: Firestore → Room ────────────────────────────────────────────────
    private suspend fun pullTransactions(uid: String) {
        firestore.collection("users/$uid/transactions").get().await()
            .documents.forEach { doc ->
                runCatching {
                    transactionDao.insertTransaction(TransactionEntity(
                        id       = doc.getLong("id") ?: 0L,
                        title    = doc.getString("title") ?: "",
                        amount   = doc.getDouble("amount") ?: 0.0,
                        type     = doc.getString("type") ?: "EXPENSE",
                        category = doc.getString("category") ?: "OTHER",
                        date     = doc.getString("date") ?: LocalDate.now().toString(),
                        note     = doc.getString("note") ?: ""
                    ))
                }
            }
    }

    private suspend fun pullSplitGroups(uid: String) {
        firestore.collection("users/$uid/split_groups").get().await()
            .documents.forEach { doc ->
                runCatching {
                    val gid = splitGroupDao.insertGroup(SplitGroupEntity(
                        id        = doc.getLong("id") ?: 0L,
                        name      = doc.getString("name") ?: "",
                        icon      = doc.getString("icon") ?: "👥",
                        createdAt = doc.getString("createdAt") ?: LocalDate.now().toString()
                    ))
                    doc.reference.collection("members").get().await().documents.forEach { m ->
                        runCatching {
                            splitMemberDao.insertMember(SplitMemberEntity(
                                id            = m.getLong("id") ?: 0L,
                                groupId       = gid,
                                name          = m.getString("name") ?: "",
                                isCurrentUser = m.getBoolean("isCurrentUser") ?: false
                            ))
                        }
                    }
                }
            }
    }
}
