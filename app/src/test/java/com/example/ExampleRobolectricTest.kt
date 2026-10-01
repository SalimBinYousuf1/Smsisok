package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.SalimDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim", appName)
    }

    @Test
    fun `test salim database operations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = SalimDatabase.getDatabase(context)
        val dao = db.salimDao()

        dao.blockNumber(
            BlockedNumberEntity(
                normalizedNumber = "+15550001111",
                rawNumber = "+1 (555) 000-1111",
                note = "Spam test"
            )
        )

        val isBlocked = dao.isNumberBlocked("+15550001111")
        assertTrue(isBlocked)

        dao.unblockNumber("+15550001111")
    }
}
