package com.wumple.composter;

import java.util.Random;

import com.wumple.util.RegistrationHelpers;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCompostBin extends BlockContainer
{
	/*
    private static final int ICON_SIDE = 0;
    private static final int ICON_TOP = 1;
    private static final int ICON_BOTTOM = 2;
    private static final int ICON_INNER = 3;

    @SideOnly(Side.CLIENT)
    IIcon[] icons;
    */

    public BlockCompostBin ()
    {
        super(Material.WOOD);
        setTickRandomly(true);
        setHardness(1.5f);
        setResistance(5f);
        
        RegistrationHelpers.nameHelper(this, "composter:compost_bin");
    }

    /*
    @Override
    public boolean renderAsNormalBlock () {
        return false;
    }
    

    @Override
    public int getRenderType () {
        return ClientProxy.compostBinRenderID;
    }

    @Override
    public boolean isOpaqueCube () {
        return false;
    }
    */

    @Override
    public boolean onBlockActivated(World world, BlockPos coords, IBlockState p_onBlockActivated_3_, EntityPlayer player, EnumHand handcontents, EnumFacing p_onBlockActivated_6_, float p_onBlockActivated_7_, float p_onBlockActivated_8_, float p_onBlockActivated_9_)
    {
    	player.openGui(Composter.instance, ComposterGuiHandler.compostBinGuiID, world, coords.getX(), coords.getY(), coords.getZ());
        return true;
    }

    public static void updateBlockState (World world, BlockPos pos) {
        TileEntityCompostBin te = (TileEntityCompostBin) world.getTileEntity(pos);
        if (te == null)
            return;

        te.markDirty();
    }

    @Override
    public void breakBlock (World world, BlockPos pos, IBlockState state)
    {
        TileEntityCompostBin te = getTileEntity(world, pos);

        if (te != null) {
            for (int i = 0; i < te.getSizeInventory(); i++) {
                ItemStack item = te.getStackInSlot(i);
                if (item != null)
                    dropBlockAsItem(world, pos, state, 0);
            }
        }

        super.breakBlock(world, pos, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick (IBlockState state, World world, BlockPos pos, Random random)
    {
        TileEntityCompostBin te = (TileEntityCompostBin) world.getTileEntity(pos);
        if (te == null)
            return;

        if (te.isDecomposing()) {
            float px = pos.getX() + .5f + random.nextFloat() * .6f - .3f;
            float py = pos.getY() + .5f + random.nextFloat() * 6f / 16f;
            float pz = pos.getZ() + .5f + random.nextFloat() * .6f - .3f;

            //world.spawnParticle("smoke", px, py, pz, 0, 0, 0);
            ParticleSteam.spawnParticle(world, px, py, pz);
        }
    }

    /*
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon (int side, int meta) {
        switch (side) {
            case 0:
                return icons[ICON_BOTTOM];
            case 1:
                return icons[ICON_TOP];
            default:
                return icons[ICON_SIDE];
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getInnerIcon () {
        return icons[ICON_INNER];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons (IIconRegister register) {
        icons = new IIcon[4];

        icons[ICON_SIDE] = register.registerIcon(GardenCore.MOD_ID + ":compost_bin_side");
        icons[ICON_TOP] = register.registerIcon(GardenCore.MOD_ID + ":compost_bin_top");
        icons[ICON_BOTTOM] = register.registerIcon(GardenCore.MOD_ID + ":compost_bin_bottom");
        icons[ICON_INNER] = register.registerIcon(GardenCore.MOD_ID + ":compost_bin_inner");
    }
    */

    @Override
    public TileEntity createNewTileEntity (World world, int meta) {
        return new TileEntityCompostBin();
    }

    public TileEntityCompostBin getTileEntity (IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return (te != null && te instanceof TileEntityCompostBin) ? (TileEntityCompostBin) te : null;
    }
}
