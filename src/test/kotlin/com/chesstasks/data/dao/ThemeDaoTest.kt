package com.chesstasks.data.dao

import com.chesstasks.data.dto.Themes
import io.ktor.test.dispatcher.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import testutils.BaseTest
import testutils.Inject
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ThemeDaoTest : BaseTest() {
    @Inject
    lateinit var themeDao: ThemeDao

    private fun setupMateTheme(id: Int = 0) = transaction {
        Themes.insert {
            it[Themes.id] = id
            it[name] = "mate"
        }
    }

    @Test
    fun `getThemeId returns NULL if theme does not exist`() = testSuspend {
        assertNull(themeDao.getThemeId("mate"))
    }

    @Test
    fun `getThemeId returns INT if theme exist`() = testSuspend {
        setupMateTheme()
        assertNotNull(themeDao.getThemeId("mate"))
    }

    @Test
    fun `getThemeId returns expected ID`() = testSuspend {
        val rid = Random.nextInt()
        setupMateTheme(id = rid)
        assertEquals(rid, themeDao.getThemeId("mate"))
    }

    @Test
    fun `insertTheme does not throw`() = testSuspend {
        assertDoesNotThrow { themeDao.insertTheme("mateIn2") }
    }

    @Test
    fun `insertTheme throws when theme already exist`() = testSuspend {
        setupMateTheme()
        assertThrows<Exception> { themeDao.insertTheme("mate") }
    }

    private fun countThemes(): Long = transaction { Themes.selectAll().count() }

    @Test
    fun `insertTheme makes Themes table bigger by 1 record`() = testSuspend {
        val bef = countThemes()
        themeDao.insertTheme("mate")
        assertEquals(bef + 1, countThemes())
    }

    private fun getThemeByName(name: String): Int? =
        transaction { Themes.select { Themes.name eq name }.singleOrNull()?.getOrNull(Themes.id) }

    private fun getThemeById(id: Int): Pair<Int, String>? = transaction {
        val row = Themes.select { Themes.id eq id }.singleOrNull() ?: return@transaction null
        row[Themes.id] to row[Themes.name]
    }

    @Test
    fun `insertTheme makes Theme in Themes table`() = testSuspend {
        assertNull(getThemeByName("mate"))
        themeDao.insertTheme("mate")
        assertNotNull(getThemeByName("mate"))
    }

    @Test
    fun `insertTheme makes Theme in Themes table and returns good record ID`() = testSuspend {
        val id = themeDao.insertTheme("mate")
        val theme = getThemeById(id)
        assertNotNull(theme)
        assertEquals(id, theme.first)
        assertEquals("mate", theme.second)
    }
}