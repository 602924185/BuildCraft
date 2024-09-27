package ct.buildcraft.energy.generation.features;

import java.util.function.Predicate;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.enums.EnumSpring;
import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.core.block.BlockSpring;
import ct.buildcraft.energy.BCEnergyFluids;
import ct.buildcraft.energy.blockEntity.TileSpringOil;
import ct.buildcraft.lib.BCLib;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;

public abstract class OilGenStructure {
    public final Box box;
    public final ReplaceType replaceType;

    public OilGenStructure(Box containingBox, ReplaceType replaceType) {
        this.box = containingBox;
        this.replaceType = replaceType;
    }
    
    public final void generate(WorldGenLevel world, Box within) {
        Box intersect = box.getIntersect(within);
        if (intersect != null) {
            generateWithin(world, intersect);
        }
        else {
        	BCLog.logger.debug("OilGenStructure:fail to generate by box "+ box +" for "+within);
        }
    }

    /** Generates this structure in the world, but only between the given coordinates. */
    protected abstract void generateWithin(WorldGenLevel world, Box intersect);

    /** @return The number of oil blocks that this structure will set. Note that this is called *after*
     *         {@link #generateWithin(Level, Box)}, by the Spring type, so this can store the number set. */
    protected abstract int countOilBlocks();

    public void setOilIfCanReplace(WorldGenLevel world, BlockPos pos) {
        if (canReplaceForOil(world, pos)) {
            setOil(world, pos);
        }
    }

    public boolean canReplaceForOil(WorldGenLevel world, BlockPos pos) {
        return replaceType.canReplace(world, pos);
    }

    public static void setOil(WorldGenLevel world, BlockPos pos) {
    	BCLog.logger.debug("OilGenStruecutre:generate oil for "+pos);
        world.setBlock(pos, BCEnergyFluids.crudeOil[0].defaultFluidState().createLegacyBlock(), 2);
    }

    public enum ReplaceType {
        ALWAYS {
            @Override
            public boolean canReplace(WorldGenLevel world, BlockPos pos) {
                return true;
            }
        },
        IS_FOR_LAKE {
            @Override
            public boolean canReplace(WorldGenLevel world, BlockPos pos) {
                return ALWAYS.canReplace(world, pos);
            }
        };
        public abstract boolean canReplace(WorldGenLevel world, BlockPos pos);
    }

    public static class GenByPredicate extends OilGenStructure {
        public final Predicate<BlockPos> predicate;

        public GenByPredicate(Box containingBox, ReplaceType replaceType, Predicate<BlockPos> predicate) {
            super(containingBox, replaceType);
            this.predicate = predicate;
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
                if (predicate.test(pos)) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        protected int countOilBlocks() {
            int count = 0;
            for (BlockPos pos : BlockPos.betweenClosed(box.min(), box.max())) {
                if (predicate.test(pos)) {
                    count++;
                }
            }
            return count;
        }
    }

    public static class FlatPattern extends OilGenStructure {
        private final boolean[][] pattern;
        private final int depth;

        private FlatPattern(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static FlatPattern create(BlockPos start, ReplaceType replaceType, boolean[][] pattern, int depth) {
            BlockPos min = start.offset(0, 1 - depth, 0);
            BlockPos max = start.offset(pattern.length - 1, 0, pattern.length == 0 ? 0 : pattern[0].length - 1);
            Box box = new Box(min, max);
            return new FlatPattern(box, replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            BlockPos start = box.min();
            for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
                int x = pos.getX() - start.getX();
                int z = pos.getZ() - start.getZ();
                if (pattern[x][z]) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        protected int countOilBlocks() {
            int count = 0;
            for (int x = 0; x < pattern.length; x++) {
                for (int z = 0; z < pattern[x].length; z++) {
                    if (pattern[x][z]) {
                        count++;
                    }
                }
            }
            return count * depth;
        }
    }

    public static class PatternTerrainHeight extends OilGenStructure {
        private final boolean[][] pattern;
        private final int depth;

        private PatternTerrainHeight(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static PatternTerrainHeight create(BlockPos start, ReplaceType replaceType, boolean[][] pattern,
            int depth) {
            BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
            BlockPos max = min.offset(pattern.length - 1, 255, pattern.length == 0 ? 0 : pattern[0].length - 1);
            Box box = new Box(min, max);
            return new PatternTerrainHeight(box, replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
                int px = x - box.min().getX();

                for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
                    int pz = z - box.min().getZ();

                    if (pattern[px][pz]) {
                        BlockPos upper = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE/*Maybe*/, new BlockPos(x, 0, z)).below();
                        if (canReplaceForOil(world, upper)) {
                            for (int y = 0; y < 5; y++) {
                                world.setBlock(upper.above(y), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                            }
                            for (int y = 0; y < depth; y++) {
                                setOilIfCanReplace(world, upper.below(y));
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected int countOilBlocks() {
            int count = 0;
            for (int x = 0; x < pattern.length; x++) {
                for (int z = 0; z < pattern[x].length; z++) {
                    if (pattern[x][z]) {
                        count++;
                    }
                }
            }
            return count * depth;
        }
    }

    public static class Spout extends OilGenStructure {
        // FIXME (AlexIIL): This won't support cubic chunks - we'll have to do this differently in compat
        // TODO: Use a terrain generator from mc terrain generation to get the height of the world
        // A hook will go in compat for help when using cubic chunks or a different type of terrain generator
        public final BlockPos start;
        public final int radius;
        public final int height;
        private int count = 0;

        public Spout(BlockPos start, ReplaceType replaceType, int radius, int height) {
            super(createBox(start), replaceType);
            this.start = start;
            this.radius = radius;
            this.height = height;
        }

        private static Box createBox(BlockPos start) {
            // Only a block 1 x 256 x 1 -- that way we area only called once.
            // FIXME: This 256 will need to be rethought for cubic chunk support
            return new Box(start, VecUtil.replaceValue(start, Axis.Y, 256));
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            count = 0;
            int segment = world.getChunk(start).getHighestSectionPosition();
            BlockPos worldTop = new BlockPos(start.getX(), segment + 16, start.getZ());
            for (int y = segment; y >= start.getY(); y--) {
                worldTop = worldTop.below();
                BlockState state = world.getBlockState(worldTop);
                if (state.isAir()) {
                    continue;
                }
                if (BlockUtil.getFluidWithoutFlowing(state) != Fluids.EMPTY) {//TO DEBUG!
                    break;
                }
                if (state.getMaterial().blocksMotion()) {
                    break;
                }
            }
            OilGenStructure tubeY = OilStructureGen.createTube(start, worldTop.getY() - start.getY(), radius, Axis.Y);
            tubeY.generate(world, tubeY.box);
            count += tubeY.countOilBlocks();
            BlockPos base = worldTop;
            for (int r = radius; r >= 0; r--) {
                // BCLog.logger.info(" - " + base + " = " + r);
                OilGenStructure struct = OilStructureGen.createTube(base, height, r, Axis.Y);
                struct.generate(world, struct.box);
                base = base.offset(0, height, 0);
                count += struct.countOilBlocks();
            }
        }

        @Override
        protected int countOilBlocks() {
            if (count == 0) {
                throw new IllegalStateException("Called countOilBlocks before calling generateWithin!");
            }
            return count;
        }
    }

    public static class Spring extends OilGenStructure {
        public final BlockPos pos;

        public Spring(BlockPos pos) {
            super(new Box(pos, pos), ReplaceType.ALWAYS);
            this.pos = pos;
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            // NO-OP (this one is called separately)
        }

        @Override
        protected int countOilBlocks() {
            return 0;
        }

        public void generate(WorldGenLevel world, int count) {
            BlockState state = BCCoreBlocks.SPRING.get().defaultBlockState();
            state = state.setValue(BlockSpring.SPRING_TYPE, EnumSpring.OIL);
            BCLog.logger.debug("OilGenStruecutre:1 generate spring for "+pos);
            world.setBlock(pos, state, 2);
            BlockEntity tile = world.getBlockEntity(pos);
            TileSpringOil spring;
            if (tile instanceof TileSpringOil) {
                spring = (TileSpringOil) tile;
                spring.totalSources = count;
            } else {
                BCLog.logger.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at " + pos);
                spring = new TileSpringOil(pos, state);
/*                spring.setLevel(world);
                world.setBlockEntity(spring);*/
            }
//          spring.totalSources = count;
            if (BCLib.DEV) {
                BCLog.logger.info("[energy.gen.oil] Generated TileSpringOil as " + System.identityHashCode(tile));
            }
        }
    }
}
