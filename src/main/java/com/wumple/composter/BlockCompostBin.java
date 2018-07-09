package com.wumple.composter;

import java.util.Random;

import com.wumple.util.RegistrationHelpers;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCompostBin extends BlockContainer
{
    // ----------------------------------------------------------------------
    // BlockCompostBin
	
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockCompostBin ()
    {
        this(Material.WOOD);
    }
    
    public BlockCompostBin (Material materialIn)
    {
        super(materialIn);
        setTickRandomly(true);
        setHardness(1.5f);
        setResistance(5f);
        setCreativeTab(CreativeTabs.MISC);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        
        RegistrationHelpers.nameHelper(this, "composter:compost_bin");
    }
        
    public static void updateBlockState (World world, BlockPos pos) 
    {
        TileEntityCompostBin te = (TileEntityCompostBin) world.getTileEntity(pos);
        if (te == null)
        {
            return;
        }

        te.markDirty();
    }
    
    public TileEntityCompostBin getTileEntity (IBlockAccess world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        return ((te != null) && (te instanceof TileEntityCompostBin)) ? (TileEntityCompostBin) te : null;
    }
    
    // from
    // http://www.minecraftforge.net/forum/topic/42458-solved1102-blockstates-crashing/?do=findComment&comment=228689
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }
    
    // ----------------------------------------------------------------------
    // Block
    
    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     * @deprecated call via {@link IBlockState#isOpaqueCube()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    /**
     * @deprecated call via {@link IBlockState#isFullCube()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT_MIPPED;
        //return BlockRenderLayer.SOLID;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick (IBlockState state, World world, BlockPos pos, Random random)
    {
        TileEntityCompostBin te = (TileEntityCompostBin) world.getTileEntity(pos);
        if (te == null)
        {
            return;
        }

        if (te.isDecomposing())
        {
            float px = pos.getX() + .5f + random.nextFloat() * .6f - .3f;
            float py = pos.getY() + .5f + random.nextFloat() * 6f / 16f;
            float pz = pos.getZ() + .5f + random.nextFloat() * .6f - .3f;

            ParticleSteam.spawnParticle(world, px, py, pz);
        }
    }
    
    // --- Block state ---
    
    // from
    // http://www.minecraftforge.net/forum/topic/62067-solved-itickable-and-tes-not-ticking/
    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    // from
    // https://stackoverflow.com/questions/34677155/minecraft-doesnt-find-blockstates-state
    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing) state.getValue(FACING)).getIndex();
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
     * 
     * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
     * 
     * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

        if (stack.hasDisplayName())
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityCompostBin)
            {
                ((TileEntityCompostBin) tileentity).setName(stack.getDisplayName());
            }
        }
    }
    
    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        IInventory iinventory = (TileEntityCompostBin) tileentity;

        if (iinventory != null)
        {
            if (worldIn.getBlockState(pos.up()).doesSideBlockChestOpening(worldIn, pos.up(), EnumFacing.DOWN))
            {
                return true;
            }
            else if (worldIn.isRemote)
            {
                return true;
            }
            else
            {
            	playerIn.openGui(Composter.instance, ComposterGuiHandler.compostBinGuiID, worldIn, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        else
        {
            return true;
        }
    }
    
    // ----------------------------------------------------------------------
    // BlockContainer
    
    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof IInventory)
        {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    // ----------------------------------------------------------------------
    // ITileEntityProvider

    @Override
    public TileEntity createNewTileEntity (World world, int meta)
    {
        return new TileEntityCompostBin();
    }
}
