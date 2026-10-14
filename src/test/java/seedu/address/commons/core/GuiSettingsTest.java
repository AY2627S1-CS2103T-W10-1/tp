package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GuiSettingsTest {
    @Test
    public void toStringMethod() {
        GuiSettings guiSettings = new GuiSettings();
        String expected = GuiSettings.class.getCanonicalName() + "{windowWidth=" + guiSettings.getWindowWidth()
                + ", windowHeight=" + guiSettings.getWindowHeight() + ", windowCoordinates="
                + guiSettings.getWindowCoordinates() + "}";
        assertEquals(expected, guiSettings.toString());
    }
    @Test
    public void coordinates_areDefensiveCopiesAndDefaultsHaveNoPosition() {
        GuiSettings settings = new GuiSettings(900, 700, 120, 80);
        Point returned = settings.getWindowCoordinates();
        returned.setLocation(0, 0);
        assertEquals(new Point(120, 80), settings.getWindowCoordinates());
        assertNull(new GuiSettings().getWindowCoordinates());
    }

    @Test
    public void equalsAndHashCode_compareEverySetting() {
        GuiSettings settings = new GuiSettings(900, 700, 120, 80);
        GuiSettings equal = new GuiSettings(900, 700, 120, 80);
        assertTrue(settings.equals(settings));
        assertEquals(settings, equal);
        assertEquals(settings.hashCode(), equal.hashCode());
        assertTrue(new HashSet<>(List.of(settings)).contains(equal));
        assertFalse(settings.equals(null));
        assertFalse(settings.equals("settings"));
        assertNotEquals(settings, new GuiSettings(901, 700, 120, 80));
        assertNotEquals(settings, new GuiSettings(900, 701, 120, 80));
        assertNotEquals(settings, new GuiSettings(900, 700, 121, 80));
        assertNotEquals(new GuiSettings(), new GuiSettings(740, 600, 0, 0));
    }

}
