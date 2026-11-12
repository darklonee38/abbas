package me.aap.fermata.ui.fragment;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/** Guards the ABBAS dashboard brand shell without coupling it to navigation behavior. */
public class DashboardBrandingResourceTest {
    @Test
    public void dashboardHeaderUsesBrandMarkAndKeepsDashboardList() throws Exception {
        String layout = read("fermata/src/main/res/layout/dashboard_fragment.xml");
        assertTrue(layout.contains("@drawable/abbas_header_mark_bg"));
        assertTrue(layout.contains("@drawable/abbas_mark"));
        assertTrue(layout.contains("@drawable/abbas_header_divider"));
        assertTrue(layout.contains("@+id/dashboard_list"));
        assertTrue(layout.contains("@string/app_name"));
    }

    @Test
    public void brandHeaderDrawablesArePresentAndValidXml() throws Exception {
        for (String path : new String[]{
                "fermata/src/main/res/drawable/abbas_header_mark_bg.xml",
                "fermata/src/main/res/drawable/abbas_header_divider.xml",
                "fermata/src/main/res/drawable/dashboard_launch_card_bg.xml",
                "fermata/src/main/res/drawable/dashboard_smart_top_bg.xml"}) {
            String xml = read(path);
            assertTrue("Missing XML declaration in " + path, xml.startsWith("<?xml"));
            assertTrue("Unclosed XML root in " + path, xml.trim().endsWith(">"));
        }
    }

    private static String read(String path) throws Exception {
        Path file = Paths.get(System.getProperty("user.dir")).resolve(path);
        if (!Files.isRegularFile(file)) {
            throw new AssertionError("Missing dashboard branding resource: " + path);
        }
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}