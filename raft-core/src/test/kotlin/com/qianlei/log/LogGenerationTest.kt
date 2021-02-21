package com.qianlei.log

import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogGenerationTest {
    @Test
    fun isValidDirName() {
        assertTrue(LogGeneration.isValidDirName("log-0"))
        assertTrue(LogGeneration.isValidDirName("log-12"))
        assertTrue(LogGeneration.isValidDirName("log-123"))
        assertFalse(LogGeneration.isValidDirName("log-"))
        assertFalse(LogGeneration.isValidDirName("foo"))
        assertFalse(LogGeneration.isValidDirName("foo-0"))
    }

    @Test
    fun testCreateFromFile() {
        val generation = LogGeneration(File("log-6"))
        assertEquals(6, generation.lastIncludedIndex)
    }

    @Test
    fun testCreateFromFileFailed() {
        assertThrows<IllegalArgumentException> { LogGeneration(File("foo-6")) }
    }

    @Test
    fun testCreateWithBaseDir() {
        val generation = LogGeneration(File("data"), 10)
        assertEquals(10, generation.lastIncludedIndex)
        assertEquals("log-10", generation.get().name)
    }

    @Test
    fun testCompare() {
        val baseDir = File("data")
        val generation = LogGeneration(baseDir, 10)
        assertEquals(1, generation.compareTo(LogGeneration(baseDir, 9)))
        assertEquals(0, generation.compareTo(LogGeneration(baseDir, 10)))
        assertEquals(-1, generation.compareTo(LogGeneration(baseDir, 11)))
    }

    @Test
    fun testGetFile() {
        val generation = LogGeneration(File("data"), 20)
        assertEquals(RootDir.FILE_NAME_SNAPSHOT, generation.getSnapshotFile().name)
        assertEquals(RootDir.FILE_NAME_ENTRIES, generation.getEntriesFile().name)
        assertEquals(RootDir.FILE_NAME_ENTRY_OFFSET_INDEX, generation.getEntryOffsetIndexFile().name)
    }
}