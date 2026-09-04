package com.lifetracker.data.repository

import com.lifetracker.data.db.NotificationPrefDao
import com.lifetracker.data.db.NotificationPrefEntity
import com.lifetracker.data.db.UserDao
import com.lifetracker.data.db.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val notificationPrefDao: NotificationPrefDao
) {
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun saveUser(user: UserEntity) = userDao.insertOrUpdate(user)

    // Notification Preferences
    fun getAllNotificationPrefs(): Flow<List<NotificationPrefEntity>> =
        notificationPrefDao.getAllPrefs()

    fun getPrefForModule(module: String): Flow<NotificationPrefEntity?> =
        notificationPrefDao.getPrefForModule(module)

    suspend fun saveNotificationPref(pref: NotificationPrefEntity) =
        notificationPrefDao.insertOrUpdate(pref)
}
