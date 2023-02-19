package data.scripts.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.ui.ptes_giveMapUI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class es_giveChip implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if (dialog == null) {
            Console.showMessage("Can only be used in market");
            return CommandResult.ERROR;
        }
        dialog.showCustomDialog(1120, 700, new ptes_giveMapUI());
        return null;
    }
}
