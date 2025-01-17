package org.valkyrienskies.eureka.blockentity

import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.Ship
import org.valkyrienskies.core.api.getAttachment
import org.valkyrienskies.core.api.saveAttachment
import org.valkyrienskies.eureka.EurekaBlockEntities
import org.valkyrienskies.eureka.block.ShipHelmBlock
import org.valkyrienskies.eureka.gui.shiphelm.ShipHelmScreenMenu
import org.valkyrienskies.eureka.ship.EurekaShipControl
import org.valkyrienskies.eureka.util.ShipAssembler
import org.valkyrienskies.mod.api.ShipBlockEntity
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.entity.ShipMountingEntity
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toDoubles
import org.valkyrienskies.mod.common.util.toJOML

class ShipHelmBlockEntity :
    BlockEntity(EurekaBlockEntities.SHIP_HELM.get()), MenuProvider, ShipBlockEntity {

    override var ship: Ship? = null
    val assembled get() = ship != null

    override fun createMenu(id: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return ShipHelmScreenMenu(id, playerInventory, this)
    }

    override fun getDisplayName(): Component {
        return Component.nullToEmpty("Ship Helm")
    }

    // Needs to get called server-side
    fun spawnSeat(blockPos: BlockPos, state: BlockState, level: ServerLevel): ShipMountingEntity {
        val newPos = blockPos.relative(state.getValue(HorizontalDirectionalBlock.FACING))
        val entity = ValkyrienSkiesMod.SHIP_MOUNTING_ENTITY_TYPE.create(level)!!.apply {
            val seatEntityPos: Vector3dc = Vector3d(newPos.x + .5, newPos.y + .5, newPos.z + .5)
            inShipPosition = seatEntityPos
            if (ship != null) {
                val posInWorld = ship!!.shipToWorld.transformPosition(seatEntityPos, Vector3d())
                moveTo(posInWorld.x, posInWorld.y, posInWorld.z)
            } else {
                moveTo(seatEntityPos.x(), seatEntityPos.y(), seatEntityPos.z())
            }

            lookAt(
                EntityAnchorArgument.Anchor.EYES,
                state.getValue(HorizontalDirectionalBlock.FACING).normal.toDoubles().add(position())
            )

            isController = true
        }

        level.addFreshEntityWithPassengers(entity)
        return entity
    }

    // Needs to get called server-side
    fun assemble() {
        val level = level as ServerLevel

        // Check the block state before assembling to avoid creating an empty ship
        val blockState = level.getBlockState(blockPos)
        if (blockState.block != ShipHelmBlock) {
            return
        }

        val ship = level.shipObjectWorld.createNewShipAtBlock(blockPos.toJOML(), false, 1.0, level.dimensionId)
        ship.saveAttachment(EurekaShipControl())
        ShipAssembler.fillShip(level, ship, blockPos)
    }

    fun align() {
        ship?.getAttachment<EurekaShipControl>()?.align()
    }

    companion object {
        val supplier = { ShipHelmBlockEntity() }
    }
}
