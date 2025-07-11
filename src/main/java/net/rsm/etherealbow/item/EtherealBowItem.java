package net.rsm.etherealbow.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.rsm.etherealbow.item.ModSounds;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class EtherealBowItem extends BowItem {
    private static final Set<UUID> playersWithSoundPlaying = new HashSet<>();
    private static final double LASER_RANGE = 50.0; // Maximum laser range
    
    public EtherealBowItem(Settings settings) {
        super(settings);
    }

    // 1) Play pull sound on the CLIENT as soon as right-clicking begins
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Only on the client, and only for your custom bow:
        if (world.isClient && !playersWithSoundPlaying.contains(user.getUuid())) {
            user.playSound(ModSounds.BOW_PULL, 1.0F, 1.0F);
            playersWithSoundPlaying.add(user.getUuid());
        }

        // This starts the draw action:
        return super.use(world, user, hand);
    }

    // 2) Shoot laser ray on release
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        
        // Clear the sound state
        if (world.isClient) {
            playersWithSoundPlaying.remove(player.getUuid());
        }

        int useTime = getMaxUseTime(stack) - remainingUseTicks;
        float pull = getPullProgress(useTime);
        if (pull < 0.1F) return;

        // Fire the laser ray
        fireLaserRay(world, player, stack, pull);
        
        // Apply durability damage
        stack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));
    }

    private void fireLaserRay(World world, PlayerEntity player, ItemStack stack, float pull) {
        // Calculate laser properties based on pull strength
        double range = LASER_RANGE * pull;
        float damage = 20.0F * pull; // Base damage scaled by pull
        
        // Apply Power enchantment
        int power = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
        if (power > 0) {
            damage += power * 3.5F + 1.25F;
        }

        // Get player's look direction
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVector();
        Vec3d end = start.add(direction.multiply(range));

        // Perform raycast for blocks
        BlockHitResult blockHit = world.raycast(new RaycastContext(
            start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player
        ));

        // Adjust end position if we hit a block
        Vec3d actualEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getPos() : end;
        double actualRange = start.distanceTo(actualEnd);

        // Find entities along the ray
        Box searchBox = new Box(start, actualEnd).expand(1.0);
        Entity hitEntity = null;
        double closestDistance = actualRange;

        for (Entity entity : world.getOtherEntities(player, searchBox)) {
            if (entity == player) continue;
            
            Box entityBox = entity.getBoundingBox().expand(0.3);
            Vec3d rayStart = start;
            Vec3d rayEnd = actualEnd;
            
            // Simple ray-box intersection
            if (entityBox.raycast(rayStart, rayEnd).isPresent()) {
                double distance = start.distanceTo(entity.getPos());
                if (distance < closestDistance) {
                    hitEntity = entity;
                    closestDistance = distance;
                    actualEnd = entity.getPos();
                }
            }
        }

        // Deal damage to hit entity
        if (hitEntity != null && !world.isClient) {
            DamageSource damageSource = player.getDamageSources().playerAttack(player);
            boolean wasCrit = pull >= 1.0F;
            float finalDamage = wasCrit ? damage * 1.5F : damage;
            
            hitEntity.damage(damageSource, finalDamage);
            
            // Apply knockback if Punch enchantment is present
            int punch = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
            if (punch > 0) {
                Vec3d knockback = direction.multiply(punch * 0.6);
                hitEntity.addVelocity(knockback.x, knockback.y, knockback.z);
            }
        }

        // Visual effects (particles)
        if (world instanceof ServerWorld serverWorld) {
            createLaserParticles(serverWorld, start, actualEnd);
        }

        // Play sound
        if (!world.isClient) {
            world.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModSounds.BOW_SHOOT,
                SoundCategory.PLAYERS,
                1.0F, 1.0F + (pull * 0.5F) // Pitch varies with pull strength
            );
        }
    }

    private void createLaserParticles(ServerWorld world, Vec3d start, Vec3d end) {
        Vec3d direction = end.subtract(start);
        double distance = direction.length();
        direction = direction.normalize();
        
        // Start particles 2 blocks in front of the player to avoid blocking view
        Vec3d particleStart = start.add(direction.multiply(1.5));
        double particleDistance = distance - 1.5; // Adjust distance accordingly
        
        // Don't create particles if the laser is too short
        if (particleDistance <= 0) return;
        
        // Create particle trail with tornado effect
        int particleCount = (int) (particleDistance * 10); // 10 particles per block
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3d pos = particleStart.add(direction.multiply(particleDistance * progress));
            
            // Main end rod particle
            world.spawnParticles(
                ParticleTypes.END_ROD, // Bright white particle
                pos.x, pos.y, pos.z,
                1, 0, 0, 0, 0
            );
            
            // Create tornado effect around the end rod trail
            double time = System.currentTimeMillis() / 1000.0; // Use time for rotation
            double radius = 0.3; // Tornado radius
            int tornadoParticles = 3; // Number of tornado particles per position
            
            for (int j = 0; j < tornadoParticles; j++) {
                // Calculate angle for each tornado particle
                double angle = (time * 3.0) + (j * Math.PI * 2.0 / tornadoParticles) + (progress * Math.PI * 4.0);
                
                // Create perpendicular vectors to the laser direction for circular motion
                Vec3d perpendicular1 = new Vec3d(0, 1, 0);
                if (Math.abs(direction.y) > 0.9) {
                    perpendicular1 = new Vec3d(1, 0, 0);
                }
                perpendicular1 = perpendicular1.crossProduct(direction).normalize();
                Vec3d perpendicular2 = direction.crossProduct(perpendicular1);
                
                // Calculate tornado particle position
                Vec3d tornadoOffset = perpendicular1.multiply(Math.cos(angle) * radius)
                    .add(perpendicular2.multiply(Math.sin(angle) * radius));
                Vec3d tornadoPos = pos.add(tornadoOffset);
                
                // Spawn blue tornado particles (using soul flame for blue color)
                world.spawnParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    tornadoPos.x, tornadoPos.y, tornadoPos.z,
                    1, 0, 0, 0, 0
                );
            }
        }
        
        // Impact particles
        world.spawnParticles(
            ParticleTypes.EXPLOSION,
            end.x, end.y, end.z,
            3, 0.5, 0.5, 0.5, 0
        );
        
        // Blue impact particles to match the tornado
        world.spawnParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            end.x, end.y, end.z,
            8, 0.3, 0.3, 0.3, 0.1
        );
    }
}