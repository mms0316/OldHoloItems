package com.klin.holoItems.interfaces.combinable;

import org.bukkit.entity.LivingEntity;

//for when an entity with modifiers is spawned
public interface Spawnable {

    void ability(LivingEntity entity, String info);
}