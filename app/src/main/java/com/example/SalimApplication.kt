package com.example

import android.app.Application
import com.example.data.local.SalimDatabase
import com.example.data.repository.ContactsRepository
import com.example.data.repository.SubscriptionRepository
import com.example.notifications.MessagingNotificationManager
import com.example.sms.RoleAndPermissionManager
import com.example.sms.SmsSender
import com.example.sms.TelephonySmsRepository

class SalimApplication : Application() {

    lateinit var database: SalimDatabase
        private set

    lateinit var contactsRepository: ContactsRepository
        private set

    lateinit var smsRepository: TelephonySmsRepository
        private set

    lateinit var subscriptionRepository: SubscriptionRepository
        private set

    lateinit var roleAndPermissionManager: RoleAndPermissionManager
        private set

    lateinit var smsSender: SmsSender
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        MessagingNotificationManager.initNotificationChannel(this)

        database = SalimDatabase.getDatabase(this)
        contactsRepository = ContactsRepository(this)
        smsRepository = TelephonySmsRepository(this, contactsRepository)
        subscriptionRepository = SubscriptionRepository(this)
        roleAndPermissionManager = RoleAndPermissionManager(this)
        smsSender = SmsSender(this)
    }

    companion object {
        lateinit var instance: SalimApplication
            private set
    }
}
