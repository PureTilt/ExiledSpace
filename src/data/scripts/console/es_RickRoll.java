package data.scripts.console;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class es_RickRoll implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        try {
            desktop.browse(new URL("https://www.youtube.com/watch?v=dQw4w9WgXcQ").toURI());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
