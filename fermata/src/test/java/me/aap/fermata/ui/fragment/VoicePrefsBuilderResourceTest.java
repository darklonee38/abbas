package me.aap.fermata.ui.fragment;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/** Keeps the voice settings entry clearly scoped to the single ABBAS assistant. */
public class VoicePrefsBuilderResourceTest {
    @Test
    public void voiceSettingsSectionHasLocalizedDescription() throws Exception {
        String builder = read("fermata/src/main/java/me/aap/fermata/ui/fragment/VoicePrefsBuilder.java");
        String english = read("fermata/src/main/res/values/strings.xml");
        String turkish = read("fermata/src/main/res/values-tr/strings.xml");

        assertTrue(builder.contains("o.subtitle = R.string.voice_control_section_sub;"));
        assertTrue(builder.contains("refresh.accept(voice);"));
        assertTrue(builder.contains("Consumer<PreferenceSet> refresh"));
        assertTrue(english.contains("voice_control_section_sub"));
        assertTrue(english.contains("existing ABBAS assistant"));
        assertTrue(turkish.contains("voice_control_section_sub"));
        assertTrue(turkish.contains("Mevcut ABBAS asistanını"));
    }

    private static String read(String path) throws Exception {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path file = workingDirectory.resolve(path);
        if (!Files.isRegularFile(file)) {
            file = workingDirectory.resolve("..").resolve(path).normalize();
        }
        if (!Files.isRegularFile(file)) {
            throw new AssertionError("Missing voice settings resource: " + path
                    + " (working directory: " + workingDirectory + ")");
        }
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
