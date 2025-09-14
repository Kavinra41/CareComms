package com.carecomms.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.carecomms.database.CareCommsDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(CareCommsDatabase.Schema, "carecomms.db")
    }
}

class IOSDatabaseDriverFactory : DatabaseDriverFactory()