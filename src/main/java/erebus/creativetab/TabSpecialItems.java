package erebus.creativetab;

import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erebus.ModItems;

public class TabSpecialItems extends CreativeTabErebus {

	public TabSpecialItems() {
		super("erebus.special");
	}

	@Override
	public Item getTabIconItem() {
		return ModItems.portalActivator;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int func_151243_f() {
		return 0;
	}
}