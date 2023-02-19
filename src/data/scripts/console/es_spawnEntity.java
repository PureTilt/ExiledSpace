package data.scripts.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ui.ptes_giveMapUI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.addSalvageEntity;

public class es_spawnEntity implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        if (args.isEmpty() || Global.getSettings().getCustomEntitySpec(args) == null){
            Console.showMessage("Wrong entity ID");
            return CommandResult.ERROR;
        } else {
            CampaignFleetAPI player = Global.getSector().getPlayerFleet();
            Global.getLogger(es_spawnEntity.class).info(args);
            CustomCampaignEntityAPI spawnedEntity = (CustomCampaignEntityAPI) addSalvageEntity(player.getStarSystem(), args, Factions.NEUTRAL);
            spawnedEntity.setLocation(player.getLocation().x,player.getLocation().y);
        }
        return null;
    }
}
