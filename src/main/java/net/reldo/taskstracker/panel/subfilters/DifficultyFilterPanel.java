package net.reldo.taskstracker.panel.subfilters;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.combattask.CombatTaskTier;
import net.reldo.taskstracker.tasktypes.league3.League3TaskTier;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class DifficultyFilterPanel extends FilterButtonPanel
{

    public DifficultyFilterPanel(TasksTrackerPlugin plugin, SpriteManager spriteManager)
    {
        super(plugin);
        this.configKey = "tierFilter";

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        redraw();
    }

    @Override
    protected JPanel makeButtonPanel()
    {
        // Panel that holds skill icons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 3));//@todo change this to a dynamic size as a taskType with more than 6 tiers will break it
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        LinkedHashMap<String, BufferedImage> skillImages = getIconImages();

        // For each skill on the in-game skill panel, create a button and add it to the UI
        skillImages.forEach((name, image) -> {
            JToggleButton button = makeButton(name, image);
            buttons.put(name, button);
            buttonPanel.add(button);
        });

        return buttonPanel;
    }

    //@todo (maybe) change this to use SpriteManager after it has apologised to me
    @Override
    protected LinkedHashMap<String, BufferedImage> getIconImages()
    {
        LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
        String tierName;
        BufferedImage tierImage;
        String directory = "panel/components/task_tiers/";

        if(plugin.selectedTaskType == null) return images;

        if (plugin.selectedTaskType.equals(TaskType.LEAGUE_3))
        {
            directory += "league3/";

            for (League3TaskTier tier : League3TaskTier.values())
            {
                tierName = tier.name().toLowerCase();
                String tierIcon = directory + tierName + ".png";
                tierImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, tierIcon);

                images.put(tierName, tierImage);
            }
        }

        if (plugin.selectedTaskType.equals(TaskType.COMBAT))
        {
            directory += "combat/";

            for (CombatTaskTier tier : CombatTaskTier.values())
            {
                tierName = tier.name().toLowerCase();
                String tierIcon = directory + tierName + ".png";
                tierImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, tierIcon);

                images.put(tierName, tierImage);
            }
        }

        return images;
    }
}
