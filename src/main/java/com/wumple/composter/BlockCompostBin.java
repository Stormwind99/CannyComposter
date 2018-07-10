package com.wumple.composter;

import java.util.Random;

import com.wumple.util.RegistrationHelpers;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCompostBin extends BlockContainer
{
    // ----------------------------------------------------------------------
    // BlockCompostBin
	
	public static final int NUM_LEVELS = 3;
	public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, NUM_LEVELS);
	
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
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(0)));
        
        RegistrationHelpers.nameHelper(this, "composter:compost_bin");
    }
        
    public static void updateBlockState (World world, BlockPos pos) 
    {
        TileEntityCompostBin te = (TileEntityCompostBin) world.getTileEntity(pos);

        if (te == null)
        {
            return;
        }

        te.updateBlockState();
    }
    
    public TileEntityCompostBin getTileEntity (IBlockAccess world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        return ((te != null) && (te instanceof TileEntityCompostBin)) ? (TileEntityCompostBin) te : null;
    }
    
    /*
    // from
    // http://www.minecraftforge.net/forum/topic/42458-solved1102-blockstates-crashing/?do=findComment&comment=228689
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }
    */
    
    public void setContentsLevel(World worldIn, BlockPos pos, IBlockState state, float amount)
    {
    	float floatLevel = amount * (float)NUM_LEVELS;
    	int level = Math.round(floatLevel);
    	
    	// make sure at least level 1 if anything is in the block
    	if (floatLevel > 0.0F)
    	{
    		level = Math.max(1, level);
    	}
    	
    	// safety - clamp within range
    	int chunkedLevel = MathHelper.clamp(level, 0, NUM_LEVELS);
    	
        worldIn.setBlockState(pos, state.withProperty(LEVEL, chunkedLevel), 2);
        worldIn.updateComparatorOutputLevel(pos, this);
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
    
    /**
     * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
     * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
     * <p>
     * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
     * does not fit the other descriptions and will generally cause other things not to connect to the face.
     * 
     * @return an approximation of the form of the given face
     * @deprecated call via {@link IBlockState#getBlockFaceShape(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
     * Implementing/overriding is fine.
     */
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        if (face == EnumFacing.UP)
        {
            return BlockFaceShape.BOWL;
        }
        else
        {
            return face == EnumFacing.DOWN ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
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

    /**
     * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
     * is fine.
     */
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return true;
    }

    /**
     * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
     * Implementing/overriding is fine.
     */
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return ((Integer)blockState.getValue(LEVEL)).intValue();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(LEVEL, Integer.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(LEVEL)).intValue();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {LEVEL});
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
    	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        //worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

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
