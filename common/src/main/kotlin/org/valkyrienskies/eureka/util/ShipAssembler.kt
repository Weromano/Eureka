package org.valkyrienskies.eureka.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import org.joml.Vector3i
import org.valkyrienskies.core.api.Ship
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.eureka.EurekaConfig
import org.valkyrienskies.mod.common.util.relocateBlock
import org.valkyrienskies.mod.common.util.toBlockPos

object ShipAssembler {
    val AIR = Blocks.AIR.defaultBlockState()

    // TODO use dense packed to send updates
    // with a more optimized algorithm for bigger ships

    fun fillShip(level: ServerLevel, ship: ShipData, center: BlockPos) {
        val shipCenter = ship.chunkClaim.getCenterBlockCoordinates(Vector3i()).toBlockPos()

        level.relocateBlock(center, shipCenter, ship)
        Direction.values()
            .forEach { forwardAxis(level, shipCenter.relative(it), center.relative(it), it, ship) }
    }

    private fun forwardAxis(
        level: ServerLevel,
        shipPos: BlockPos,
        pos: BlockPos,
        direction: Direction,
        ship: Ship
    ) {
        var pos = pos
        var shipPos = shipPos
        var blockState = level.getBlockState(pos)
        var depth = 0

        while (!EurekaConfig.SERVER.blockBlacklist.contains(Registry.BLOCK.getKey(blockState.block).toString())) {
            level.relocateBlock(pos, shipPos, ship)
            depth++

            Direction.values().filter { it != direction && it != direction.opposite }
                .forEach { forwardAxis(level, shipPos.relative(it), pos.relative(it), it, ship) }

            pos = pos.relative(direction)
            shipPos = shipPos.relative(direction)
            blockState = level.getBlockState(pos)
        }

        /*
        pos = pos.relative(direction.opposite, depth)

        repeat(depth) {
            val from1 = pos.relative(direction, it + 1)
            val from2 = pos.relative(direction, it + 1)
            level.neighborChanged(pos.relative(direction, it), level.getBlockState(from1).block, from1)
            level.neighborChanged(pos.relative(direction, it), level.getBlockState(from2).block, from2)
        }
        */
    }
}
