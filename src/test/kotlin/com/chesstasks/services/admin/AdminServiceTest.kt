package com.chesstasks.services.admin

import com.chesstasks.data.dto.Admins
import com.chesstasks.data.dto.Users
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.koin.java.KoinJavaComponent.inject
import testutils.BaseWebTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AdminServiceTest : BaseWebTest() {
    private fun setupAdmins() {
        transaction { 
            Users.insert { 
                it[id] = 0
                it[passwordHash] = ""
                it[emailAddress] = ""
                it[username] = ""
            }
            
            Admins.insert { 
                it[id] = 0
                it[userId] = 0
                it[createdAt] = 5000
            }
        }
    }
    
    @Test
    fun `getById returns expected data from database`() = testSuspend {
        setupAdmins()
        val adminService by inject<AdminService>(AdminService::class.java)
        
        val result = adminService.getById(0)
        assertEquals(0, result?.userId)
        assertEquals(5000, result?.createdAt)
    }
    
    @Test
    fun `getById returns null if expected`() = testSuspend {
        setupAdmins()
        val adminService by inject<AdminService>(AdminService::class.java)
        val result = adminService.getById(-1)
        assertNull(result)
    }
}