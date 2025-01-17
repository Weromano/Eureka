package org.valkyrienskies.eureka.block

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import org.valkyrienskies.eureka.EurekaProperties.HEAT
import org.valkyrienskies.eureka.blockentity.EngineBlockEntity
import java.util.Random

object EngineBlock : BaseEntityBlock(
    Properties.of(Material.STONE)
        .requiresCorrectToolForDrops()
        .strength(3.5F, 1200.0f)
        .sound(SoundType.STONE)
        .lightLevel { state -> if (state.getValue(HEAT) > 0) state.getValue(HEAT) + 9 else 0 }
) {
    init {
        registerDefaultState(
            this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HEAT, 0)
        )
    }

    override fun newBlockEntity(blockGetter: BlockGetter): BlockEntity = EngineBlockEntity()

    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        val blockEntity = level.getBlockEntity(pos) as EngineBlockEntity

        player.openMenu(blockEntity)

        return InteractionResult.CONSUME
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder
            .add(FACING)
            .add(HEAT)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        return defaultBlockState()
            .setValue(FACING, ctx.horizontalDirection.opposite)
    }

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    @Environment(value = EnvType.CLIENT)
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: Random) {
        val heat = state.getValue(HEAT)
        if (heat == 0) return

        val d = pos.x.toDouble() + 0.5
        val e = pos.y.toDouble()
        val f = pos.z.toDouble() + 0.5

        if (random.nextDouble() < (0.04 * heat)) {
            level.playLocalSound(d, e, f, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0f, 1.0f, false)
        }

        // Make the amount of particles based of the heat
        if (random.nextDouble() > (0.2 * heat)) return

        val direction = state.getValue(FACING)
        val axis = direction.axis
        val h = random.nextDouble() * 0.6 - 0.3
        val i = if (axis === Direction.Axis.X) direction.stepX.toDouble() * 0.52 else h
        val j = random.nextDouble() * 4.0 / 16.0
        val k = if (axis === Direction.Axis.Z) direction.stepZ.toDouble() * 0.52 else h
        level.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0, 0.0, 0.0)
        level.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0, 0.0, 0.0)
    }
}
