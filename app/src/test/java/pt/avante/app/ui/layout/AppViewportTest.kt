package pt.avante.app.ui.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class AppViewportTest {

    @Test
    fun `widths under 600dp are compact`() {
        val viewport = AppViewport(width = 599.dp, height = 800.dp)
        assertEquals(true, viewport.isCompactWidth)
        assertEquals(false, viewport.isMediumWidth)
        assertEquals(false, viewport.isExpandedWidth)
        assertEquals(false, viewport.useNavigationRail)
    }

    @Test
    fun `widths between 600dp and 839dp are medium`() {
        val viewportLow = AppViewport(width = 600.dp, height = 800.dp)
        assertEquals(false, viewportLow.isCompactWidth)
        assertEquals(true, viewportLow.isMediumWidth)
        assertEquals(false, viewportLow.isExpandedWidth)
        assertEquals(true, viewportLow.useNavigationRail)

        val viewportHigh = AppViewport(width = 839.dp, height = 800.dp)
        assertEquals(false, viewportHigh.isCompactWidth)
        assertEquals(true, viewportHigh.isMediumWidth)
        assertEquals(false, viewportHigh.isExpandedWidth)
        assertEquals(true, viewportHigh.useNavigationRail)
    }

    @Test
    fun `widths 840dp and above are expanded`() {
        val viewportLow = AppViewport(width = 840.dp, height = 800.dp)
        assertEquals(false, viewportLow.isCompactWidth)
        assertEquals(false, viewportLow.isMediumWidth)
        assertEquals(true, viewportLow.isExpandedWidth)
        assertEquals(true, viewportLow.useNavigationRail)

        val viewportHigh = AppViewport(width = 1200.dp, height = 800.dp)
        assertEquals(false, viewportHigh.isCompactWidth)
        assertEquals(false, viewportHigh.isMediumWidth)
        assertEquals(true, viewportHigh.isExpandedWidth)
        assertEquals(true, viewportHigh.useNavigationRail)
    }

    @Test
    fun `heights under 480dp are compact`() {
        val viewport = AppViewport(width = 800.dp, height = 479.dp)
        assertEquals(true, viewport.isCompactHeight)
    }

    @Test
    fun `landscape is correctly identified`() {
        val landscapeViewport = AppViewport(width = 800.dp, height = 400.dp)
        assertEquals(true, landscapeViewport.isLandscape)
        
        val portraitViewport = AppViewport(width = 400.dp, height = 800.dp)
        assertEquals(false, portraitViewport.isLandscape)
    }

    @Test
    fun `useWideReader requires both width and height`() {
        val wideReaderViewport = AppViewport(width = 600.dp, height = 480.dp)
        assertEquals(true, wideReaderViewport.useWideReader)

        val narrowViewport = AppViewport(width = 599.dp, height = 800.dp)
        assertEquals(false, narrowViewport.useWideReader)

        val shortViewport = AppViewport(width = 800.dp, height = 479.dp)
        assertEquals(false, shortViewport.useWideReader)
    }
}
