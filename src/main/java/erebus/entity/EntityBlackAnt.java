package erebus.entity;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.world.World;
import erebus.Erebus;
import erebus.ModItems;
import erebus.core.helper.Utils;
import erebus.core.proxy.CommonProxy;
import erebus.entity.ai.EntityAIAntBonemealCrops;
import erebus.entity.ai.EntityAIAntHarvestCrops;
import erebus.entity.ai.EntityAIAntPlantCrops;

public class EntityBlackAnt extends EntityTameable implements IInventory {

	private final EntityAIPanic aiPanic = new EntityAIPanic(this, 0.8D);
	private final EntityAIAntHarvestCrops aiHarvestCrops = new EntityAIAntHarvestCrops(this, 0.6D, 1);
	private final EntityAIAntPlantCrops aiPlantCrops = new EntityAIAntPlantCrops(this, 0.6D, 4);
	private final EntityAIAntBonemealCrops aiBonemealCrops = new EntityAIAntBonemealCrops(this, 0.6D, 4);
	private final EntityAIWander aiWander = new EntityAIWander(this, 0.6D);

	public boolean setAttributes; // needed for logic later
	public boolean canPickupItems;
	public boolean canCollectFromSilo;
	public boolean canAddToSilo;
	public boolean canBonemeal;

	protected ItemStack[] inventory;
	public static final int TOOL_SLOT = 0;
	public static final int CROP_ID_SLOT = 1;
	public static final int INVENTORY_SLOT = 2;

	public EntityBlackAnt(World world) {
		super(world);
		stepHeight = 1.0F;
		setAttributes = false;
		canPickupItems = false;
		canAddToSilo = false;
		canCollectFromSilo = false;
		canBonemeal = false;
		setSize(0.9F, 0.4F);
		getNavigator().setAvoidsWater(true);
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, aiWander);
		tasks.addTask(2, aiPanic);
		tasks.addTask(3, new EntityAILookIdle(this));
		inventory = new ItemStack[3];
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(24, new Integer(0));
		dataWatcher.addObject(25, new Integer(0));
		dataWatcher.addObject(26, new Integer(0));
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.6D);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D);
		//getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

	@Override
	protected String getLivingSound() {
		return "erebus:fireantsound";
	}

	@Override
	protected String getHurtSound() {
		return "erebus:fireanthurt";
	}

	@Override
	protected String getDeathSound() {
		return "erebus:squish";
	}

	@Override
	protected void func_145780_a(int x, int y, int z, Block block) {
		playSound("mob.spider.step", 0.15F, 1.0F);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 5;
	}

	@Override
	protected boolean canDespawn() {
		if (isTamed())
			return false;
		else
			return true;
	}

	@Override
	public boolean isTamed() {
		return dataWatcher.getWatchableObjectByte(16) != 0;
	}

	private static final String[] names = { "Antwan", "George", "Geoff", "Alberto", "Jose", "Linda", "Chantelle", "Dave", "Basil", "Gertrude", "Herbert", "Russel", "Adam", "Gwen", "Billy Bob Joe Bob Joe Harrison Jr.", "Sid", "Dylan", "Jade" };

	@Override
	public void setTamed(boolean tamed) {
		if (tamed) {
			dataWatcher.updateObject(16, Byte.valueOf((byte) 1));
			if (!hasCustomNameTag())
				setCustomNameTag(names[worldObj.rand.nextInt(names.length)]);
		} else
			dataWatcher.updateObject(16, Byte.valueOf((byte) 0));
	}

	public void openGUI(EntityPlayer player) {
		player.openGui(Erebus.instance, CommonProxy.GUI_ID_ANT_INVENTORY, player.worldObj, getEntityId(), 0, 0);
	}

	@Override
	public boolean interact(EntityPlayer player) {
		ItemStack is = player.inventory.getCurrentItem();

		if (is != null && is.getItem() == ModItems.antTamingAmulet && is.hasTagCompound() && is.stackTagCompound.hasKey("homeX")) {
			setDropPoint(is.getTagCompound().getInteger("homeX"), is.getTagCompound().getInteger("homeY"), is.getTagCompound().getInteger("homeZ"));
			player.swingItem();
			setTamed(true);
			playTameEffect(true);
			return true;
		}
		if (isTamed()) {
			openInventory();
			openGUI(player);
		}
		return super.interact(player);
	}

	public void setBlockHarvested(Block block, int meta) {
		Random rand = new Random();
		if (block != null) {
			Utils.dropStack(worldObj, (int) posX, (int) posY, (int) posZ, new ItemStack(block.getItemDropped(meta, rand, 0), rand.nextInt(2) + 1));
			Utils.dropStack(worldObj, (int) posX, (int) posY, (int) posZ, new ItemStack(block.getItemDropped(0, rand, 0), rand.nextInt(2) + 1));
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!worldObj.isRemote && !setAttributes) {
			openInventory();
			closeInventory();
			setAttributes = true;
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (canPickupItems) {
			EntityItem entityitem = getClosestEntityItem(this, 16.0D);
			if (entityitem != null && getStackInSlot(CROP_ID_SLOT) != null && entityitem.getEntityItem().getItem() == getStackInSlot(CROP_ID_SLOT).getItem()) {
				ItemStack stack = entityitem.getEntityItem();
				int metadata = stack.getItemDamage();
				if (metadata == getStackInSlot(CROP_ID_SLOT).getItemDamage()) {
					float distance = entityitem.getDistanceToEntity(this);
					if (distance >= 1.5F && entityitem.delayBeforeCanPickup <= 0 && !entityitem.isDead) {
						double x = entityitem.posX;
						double y = entityitem.posY;
						double z = entityitem.posZ;
						getLookHelper().setLookPosition(x, y, z, 20.0F, 8.0F);
						moveToItem(entityitem);
						return;
					}
					if (distance < 1.5F && entityitem != null) {
						getMoveHelper().setMoveTo(entityitem.posX, entityitem.posY,entityitem.posZ, 0.5D);
						addToInventory(new ItemStack(stack.getItem(), stack.stackSize, metadata));
						entityitem.setDead();
						return;
					}
				}
			}
		}

		if (getStackInSlot(TOOL_SLOT) != null && getStackInSlot(TOOL_SLOT).getItem() instanceof ItemBucket)
			if (getStackInSlot(INVENTORY_SLOT) != null && getStackInSlot(INVENTORY_SLOT).stackSize > 15) {
				canAddToSilo = true;
				canPickupItems = false;
			}

		if (!canPickupItems && canAddToSilo) {
			moveToSilo();
			Block block = worldObj.getBlock(getDropPointX(), getDropPointY(), getDropPointZ());
			if (block == Blocks.chest)
				if (getDistance(getDropPointX() + 0.5D, getDropPointY(), getDropPointZ() + 0.5D) < 1.5D) {
					addDropToInventory(getDropPointX(), getDropPointY(), getDropPointZ());
					canAddToSilo = false;
					canPickupItems = true;
				}
		}
		
		if (getStackInSlot(TOOL_SLOT) != null && getStackInSlot(TOOL_SLOT).getItem() instanceof ItemHoe || getStackInSlot(TOOL_SLOT) != null && getStackInSlot(TOOL_SLOT).getItem() instanceof ItemSpade)
			if (getStackInSlot(INVENTORY_SLOT) == null) {
				canCollectFromSilo = true; // this stops the planting AI and makes the ant go to the silo
			}

		if (canCollectFromSilo) {
			moveToSilo();
			Block block = worldObj.getBlock(getDropPointX(), getDropPointY(), getDropPointZ());
			ItemStack stack = new ItemStack(Items.wheat_seeds, 64, 0);//test stack
			if (block == Blocks.chest) {
				if (getDistance(getDropPointX() + 0.5D, getDropPointY(), getDropPointZ() + 0.5D) < 1.5D) {
					if(canBonemeal)
						stack = new ItemStack(Items.dye, 64, 15);
					//TODO add stack from chest inventory matching filter slot to ant inventory slot
					addToInventory(stack);//test stack
					canCollectFromSilo = false;
				}
			}
		}
	}
	
	private void addToInventory(ItemStack stack) {
		if (stack == null)
			return;

		if (inventory[INVENTORY_SLOT] == null)
			inventory[INVENTORY_SLOT] = stack.copy();
		else if (Utils.areStacksTheSame(stack, inventory[INVENTORY_SLOT], false) && inventory[INVENTORY_SLOT].getMaxStackSize() >= inventory[INVENTORY_SLOT].stackSize + stack.stackSize)
			inventory[INVENTORY_SLOT].stackSize += stack.stackSize;
		 }

	private void addDropToInventory(int x, int y, int z) {
		ItemStack stack = getStackInSlot(INVENTORY_SLOT);
		if (getStackInSlot(INVENTORY_SLOT) != null)
			Utils.addItemStackToInventory(Utils.getTileEntity(worldObj, x, y, z, IInventory.class), new ItemStack(stack.getItem(), stack.stackSize, stack.getItemDamage()));
		setInventorySlotContents(2, null);
	}

	@SuppressWarnings("unchecked")
	public EntityItem getClosestEntityItem(Entity entity, double d) {
		double d1 = -1.0D;
		EntityItem entityitem = null;
		List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(d, d, d));
		for (int k = 0; k < list.size(); k++) {
			Entity entity1 = list.get(k);
			if (entity1 != null && entity1 instanceof EntityItem && getStackInSlot(CROP_ID_SLOT) != null) {
				EntityItem entityitem1 = (EntityItem) entity1;
				if (entityitem1.getEntityItem().getItem() == getStackInSlot(CROP_ID_SLOT).getItem())
					if (entityitem1.getEntityItem().getItemDamage() == getStackInSlot(CROP_ID_SLOT).getItemDamage()) {
						double d2 = entityitem1.getDistanceSq(entity.posX, entity.posY, entity.posZ);
						if ((d < 0.0D || d2 < d * d) && (d1 == -1.0D || d2 < d1)) {
							d1 = d2;
							entityitem = entityitem1;
						}
					}
			}
		}
		return entityitem;
	}

	public void moveToItem(Entity entity) {
		PathEntity pathentity = worldObj.getPathEntityToEntity(this, entity, 16.0F, true, false, false, true);
		if (pathentity != null) {
			setPathToEntity(pathentity);
			getNavigator().setPath(pathentity, 0.5D);
		}
	}

	public void moveToSilo() {
		PathEntity pathentity = worldObj.getEntityPathToXYZ(this, getDropPointX(), getDropPointY() + 1, getDropPointZ(), 16.0F, true, false, false, true);
		if (pathentity != null) {
			setPathToEntity(pathentity);
			getNavigator().setPath(pathentity, 0.5D);
		}		
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		if (inventory[slot] != null) {
			ItemStack itemstack;
			if (inventory[slot].stackSize <= size) {
				itemstack = inventory[slot];
				inventory[slot] = null;
				return itemstack;
			} else {
				itemstack = inventory[slot].splitStack(size);
				if (inventory[slot].stackSize == 0)
					inventory[slot] = null;
				return itemstack;
			}
		} else
			return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (inventory[slot] != null) {
			ItemStack itemstack = inventory[slot];
			inventory[slot] = null;
			return itemstack;
		} else
			return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;

		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
	}

	@Override
	public String getInventoryName() {
		return "container.antInventory";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public final boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == TOOL_SLOT)
			return stack.getItem() == Items.shears || stack.getItem() == Items.bucket || stack.getItem() instanceof ItemHoe || stack.getItem() instanceof ItemSpade;

		return false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		if (nbt.getByte("tamed") == 1)
			setTamed(true);
		else
			setTamed(false);

		setDropPoint(nbt.getInteger("dropPointX"), nbt.getInteger("dropPointY"), nbt.getInteger("dropPointZ"));

		NBTTagList tags = nbt.getTagList("Items", 10);
		inventory = new ItemStack[getSizeInventory()];

		for (int i = 0; i < tags.tagCount(); i++) {
			NBTTagCompound data = tags.getCompoundTagAt(i);
			int j = data.getByte("Slot") & 255;

			if (j >= 0 && j < inventory.length)
				inventory[j] = ItemStack.loadItemStackFromNBT(data);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		if (isTamed())
			nbt.setByte("tamed", Byte.valueOf((byte) 1));
		else
			nbt.setByte("tamed", Byte.valueOf((byte) 0));

		nbt.setInteger("dropPointX", getDropPointX());
		nbt.setInteger("dropPointY", getDropPointY());
		nbt.setInteger("dropPointZ", getDropPointZ());

		NBTTagList tags = new NBTTagList();

		for (int i = 0; i < inventory.length; i++)
			if (inventory[i] != null) {
				NBTTagCompound data = new NBTTagCompound();
				data.setByte("Slot", (byte) i);
				inventory[i].writeToNBT(data);
				tags.appendTag(data);
			}

		nbt.setTag("Items", tags);
	}

	@Override
	public void openInventory() {
		if (worldObj.isRemote)
			return;
		canPickupItems = false;
		canAddToSilo = false;
		canCollectFromSilo = false;
		tasks.removeTask(aiWander);
		tasks.removeTask(aiPlantCrops);
		tasks.removeTask(aiHarvestCrops);
		tasks.removeTask(aiBonemealCrops);
	}

	@Override
	public void closeInventory() {
		if (worldObj.isRemote)
			return;

		if (getStackInSlot(TOOL_SLOT) == null) {
			tasks.addTask(1, aiWander);
			canPickupItems = false;
		}

		if (getStackInSlot(TOOL_SLOT) != null && getStackInSlot(TOOL_SLOT).getItem() instanceof ItemHoe) {
			tasks.addTask(1, aiPlantCrops);
			canPickupItems = false;
			canBonemeal = false;
		}

		if (getStackInSlot(TOOL_SLOT) != null && getStackInSlot(TOOL_SLOT).getItem() instanceof ItemBucket) {
			canPickupItems = true;
		}

		if (getStackInSlot(TOOL_SLOT) != null && getStackInSlot(TOOL_SLOT).getItem() instanceof ItemShears) {
			canPickupItems = false;
			tasks.addTask(1, aiHarvestCrops);
		}
		
		if (getStackInSlot(TOOL_SLOT) != null && getStackInSlot(TOOL_SLOT).getItem() instanceof ItemSpade) {
			canPickupItems = false;
			canBonemeal = true;
			tasks.addTask(1, aiBonemealCrops);
		}
		updateAITasks();
	}

	@Override
	public void markDirty() {
	}

	public void setDropPoint(int x, int y, int z) {
		dataWatcher.updateObject(24, Integer.valueOf(x));
		dataWatcher.updateObject(25, Integer.valueOf(y));
		dataWatcher.updateObject(26, Integer.valueOf(z));
	}

	public int getDropPointX() {
		return dataWatcher.getWatchableObjectInt(24);
	}

	public int getDropPointY() {
		return dataWatcher.getWatchableObjectInt(25);
	}

	public int getDropPointZ() {
		return dataWatcher.getWatchableObjectInt(26);
	}

	@Override
	public EntityAgeable createChild(EntityAgeable baby) {
		return null;
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return false;
	}
}