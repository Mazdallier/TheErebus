package erebus.core.handler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import erebus.ModAchievements;
import erebus.ModItems;

public class EntityPickupEventHandler {
	@SubscribeEvent
	public void itemPickup(EntityItemPickupEvent event) {
		if (event.item.getEntityItem() == new ItemStack(ModItems.spiderTShirt))
			event.entityPlayer.triggerAchievement(ModAchievements.tshirt);
	}
}
