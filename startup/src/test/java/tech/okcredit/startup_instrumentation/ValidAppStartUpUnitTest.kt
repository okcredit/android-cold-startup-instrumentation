package tech.okcredit.startup_instrumentation

import org.junit.Test

import org.junit.Assert.*

class ValidAppStartUpUnitTest {


    @Test
    fun `should valid only all values are present`() {
        AppStartUpTrace.processForkTime = 100
        AppStartUpTrace.contentProviderStartedTime = 200
        AppStartUpTrace.appOnCreateTime = 300
        AppStartUpTrace.appOnCreateEndTime = 400
        AppStartUpTrace.firstDrawTime = 500

        assertEquals(AppStartUpTrace.isValidAppStartUpMeasure(), true)
    }

    @Test
    fun `should not be valid if processForkTime is missing`() {
        AppStartUpTrace.processForkTime = 0
        AppStartUpTrace.contentProviderStartedTime = 200
        AppStartUpTrace.appOnCreateTime = 300
        AppStartUpTrace.appOnCreateEndTime = 400
        AppStartUpTrace.firstDrawTime = 500

        assertEquals(AppStartUpTrace.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if contentProviderStartedTime is missing`() {
        AppStartUpTrace.processForkTime = 100
        AppStartUpTrace.contentProviderStartedTime = 0
        AppStartUpTrace.appOnCreateTime = 300
        AppStartUpTrace.appOnCreateEndTime = 400
        AppStartUpTrace.firstDrawTime = 500

        assertEquals(AppStartUpTrace.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if appOnCreateTime is missing`() {
        AppStartUpTrace.processForkTime = 100
        AppStartUpTrace.contentProviderStartedTime = 200
        AppStartUpTrace.appOnCreateTime = 0
        AppStartUpTrace.appOnCreateEndTime = 400
        AppStartUpTrace.firstDrawTime = 500

        assertEquals(AppStartUpTrace.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if appOnCreateEndTime is missing`() {
        AppStartUpTrace.processForkTime = 100
        AppStartUpTrace.contentProviderStartedTime = 200
        AppStartUpTrace.appOnCreateTime = 300
        AppStartUpTrace.appOnCreateEndTime = 0
        AppStartUpTrace.firstDrawTime = 500

        assertEquals(AppStartUpTrace.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if firstDrawTime is missing`() {
        AppStartUpTrace.processForkTime = 100
        AppStartUpTrace.contentProviderStartedTime = 200
        AppStartUpTrace.appOnCreateTime = 300
        AppStartUpTrace.appOnCreateEndTime = 500
        AppStartUpTrace.firstDrawTime = 0

        assertEquals(AppStartUpTrace.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if startUp time is more than 30 sec`() {
        AppStartUpTrace.processForkTime = 100
        AppStartUpTrace.contentProviderStartedTime = 200
        AppStartUpTrace.appOnCreateTime = 300
        AppStartUpTrace.appOnCreateEndTime = 500
        AppStartUpTrace.firstDrawTime = 50000

        assertEquals(AppStartUpTrace.isValidAppStartUpMeasure(), false)
    }
}