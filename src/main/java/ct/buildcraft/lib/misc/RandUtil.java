package ct.buildcraft.lib.misc;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

/** Utilities based around more complex (but common) usages of {@link RandomSource}. */
public class RandUtil {
    /** Creates a {@link RandomSource} instance for a specific generator, for the specified chunk, in the specified world.
     * 
     * @param world The world to generate for.
     * @param chunkX The chunk X co-ord to generate for.
     * @param chunkY The chunk X co-ord to generate for.
     * @param magicNumber The magic number, specific to the generator. Each different generator that calls this should
     *            have a different number, so that different generators don't start by generating structures in the same
     *            place. It is recommended that you generate a random number once, and place it statically in the
     *            generator class (Perhaps by using <code>new SecureRandom().nextLong()</code>).
     * @return A {@link Random} instance that starts off with the same seed given the same arguments. */
    public static RandomSource createRandomForChunk(WorldGenLevel world, int chunkX, int chunkY, long magicNumber) {
        long worldSeed = world.getSeed();
        return createRandomForChunk(worldSeed, chunkX, chunkY, magicNumber);
    }

    /** Creates a {@link RandomSource} instance for a specific generator, for the specified chunk, for a given world seed
     * 
     * @param worldSeed The seed of a world to generate for.
     * @param chunkX The chunk X co-ord to generate for.
     * @param chunkY The chunk X co-ord to generate for.
     * @param magicNumber The magic number, specific to the generator. Each different generator that calls this should
     *            have a different number, so that different generators don't start by generating structures in the same
     *            place. It is recommended that you generate a random number once, and place it statically in the
     *            generator class (Perhaps by using <code>new SecureRandom().nextLong()</code>).
     * @return A {@link RandomSource} instance that starts off with the same seed given the same arguments. */
    public static RandomSource createRandomForChunk(long worldSeed, int chunkX, int chunkY, long magicNumber) {
        // Ensure we have the same seed for the same chunk
    	RandomSource worldRandom = RandomSource.create(worldSeed);
        long xSeed = worldRandom.nextLong() >> 2 + 1L;
        long zSeed = worldRandom.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * chunkX + zSeed * chunkY) ^ worldSeed;
        // XOR our own number so that we differ from other generators
        chunkSeed ^= magicNumber;
        return RandomSource.create(chunkSeed);
    }
}
