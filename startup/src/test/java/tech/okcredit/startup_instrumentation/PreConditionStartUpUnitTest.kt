package tech.okcredit.startup_instrumentation

import org.junit.Assert.*
import org.junit.Test
import tech.okcredit.startup_instrumentation.internals.PreConditionStartUp

class PreConditionStartUpUnitTest {

    @Test
    fun `should valid only all values are present`() {
        AppStartUpTracer.processForkTime = 100
        AppStartUpTracer.contentProviderStartedTime = 200
        AppStartUpTracer.appOnCreateTime = 300
        AppStartUpTracer.appOnCreateEndTime = 400
        AppStartUpTracer.firstDrawTime = 500

        assertEquals(PreConditionStartUp.isValidAppStartUpMeasure(), true)
    }

    @Test
    fun `should not be valid if processForkTime is missing`() {
        AppStartUpTracer.processForkTime = 0
        AppStartUpTracer.contentProviderStartedTime = 200
        AppStartUpTracer.appOnCreateTime = 300
        AppStartUpTracer.appOnCreateEndTime = 400
        AppStartUpTracer.firstDrawTime = 500

        assertEquals(PreConditionStartUp.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if contentProviderStartedTime is missing`() {
        AppStartUpTracer.processForkTime = 100
        AppStartUpTracer.contentProviderStartedTime = 0
        AppStartUpTracer.appOnCreateTime = 300
        AppStartUpTracer.appOnCreateEndTime = 400
        AppStartUpTracer.firstDrawTime = 500

        assertEquals(PreConditionStartUp.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if appOnCreateTime is missing`() {
        AppStartUpTracer.processForkTime = 100
        AppStartUpTracer.contentProviderStartedTime = 200
        AppStartUpTracer.appOnCreateTime = 0
        AppStartUpTracer.appOnCreateEndTime = 400
        AppStartUpTracer.firstDrawTime = 500

        assertEquals(PreConditionStartUp.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if appOnCreateEndTime is missing`() {
        AppStartUpTracer.processForkTime = 100
        AppStartUpTracer.contentProviderStartedTime = 200
        AppStartUpTracer.appOnCreateTime = 300
        AppStartUpTracer.appOnCreateEndTime = 0
        AppStartUpTracer.firstDrawTime = 500

        assertEquals(PreConditionStartUp.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if firstDrawTime is missing`() {
        AppStartUpTracer.processForkTime = 100
        AppStartUpTracer.contentProviderStartedTime = 200
        AppStartUpTracer.appOnCreateTime = 300
        AppStartUpTracer.appOnCreateEndTime = 500
        AppStartUpTracer.firstDrawTime = 0

        assertEquals(PreConditionStartUp.isValidAppStartUpMeasure(), false)
    }

    @Test
    fun `should not be valid if startUp time is more than 30 sec`() {
        AppStartUpTracer.processForkTime = 100
        AppStartUpTracer.contentProviderStartedTime = 200
        AppStartUpTracer.appOnCreateTime = 300
        AppStartUpTracer.appOnCreateEndTime = 500
        AppStartUpTracer.firstDrawTime = 50000

        assertEquals(PreConditionStartUp.isValidAppStartUpMeasure(), false)
    }
}
